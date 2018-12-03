package com.ooftf.docking.compiler;

import com.google.auto.service.AutoService;
import com.ooftf.docking.annotation.AppShip;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.ooftf.docking.compiler.Consts.KEY_MODULE_NAME;
import static com.ooftf.docking.compiler.Consts.SEPARATOR;
import static com.ooftf.docking.compiler.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.ooftf.docking.annotation.AppShip"})
public class AppShipProcessor extends AbstractProcessor {
    private List<Element> AppShips = new ArrayList<>();
    // File util, write class file into disk.
    private Filer mFiler;
    private Elements elementUtil;
    // Module name, maybe its 'app' or others
    private String moduleName = null;
    private TypeMirror iApplication = null;
    Messager messager;

    /**
     * Initializes the processor with the processing environment by
     * setting the {@code processingEnv} field to the value of the
     * {@code processingEnv} argument.  An {@code
     * IllegalStateException} will be thrown if this method is called
     * more than once on the same object.
     *
     * @param processingEnv environment to access facilities the tool framework
     *                      provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        /**
         * 初始化需要用到的工具
         */
        // Generate class.  用于生成java文件
        mFiler = processingEnv.getFiler();
        // Get class meta.
        elementUtil = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");

        } else {
            throw new RuntimeException("ARouter::Compiler >>> No module name, for more information, look at gradle log.");
        }
        //添加注解@AppShip 需要实现的接口
        iApplication = elementUtil.getTypeElement(Consts.IAPPLICATION).asType();

    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AppShip.class);
            try {
                parseAppShips(elements);
            } catch (Exception e) {
                log(e.getMessage());
            }
            return true;
        }

        return false;
    }

    /**
     * Parse tollgate.
     *
     * @param elements elements of tollgate.
     */
    private void parseAppShips(Set<? extends Element> elements) throws IOException {
        if (CollectionUtils.isNotEmpty(elements)) {
            // Verify and cache, sort incidentally.
            for (Element element : elements) {
                // Check the AppShip meta
                if (verify(element)) {
                    AppShips.add(element);
                }
            }
            buildJavaClass();
        }
    }

    private void buildJavaClass() throws IOException {
        // 生成类需要实现的接口
        TypeElement classInterface = elementUtil.getTypeElement("com.ooftf.docking.api.IAppShipRegister");
        // 构建参数
        ParameterSpec listApplicationParam = buildParam();


        // Build method : 'register'
        MethodSpec.Builder loadIntoMethodOfTollgateBuilder = MethodSpec.methodBuilder("register")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(listApplicationParam);

        // Generate
        if (null != AppShips && AppShips.size() > 0) {
            // Build method body
            for (Element e : AppShips) {
                loadIntoMethodOfTollgateBuilder.addStatement("applications.add(new " + e.getSimpleName() + "());");
            }
        }

        // Write to disk(Write file even AppShips is empty.)
        JavaFile.builder(Consts.REGISTER_PACKAGE_NAME,
                TypeSpec.classBuilder("Docking" + SEPARATOR + "AppShips" + SEPARATOR + moduleName)
                        .addModifiers(PUBLIC)
                        .addJavadoc(WARNING_TIPS)
                        .addMethod(loadIntoMethodOfTollgateBuilder.build())
                        .addSuperinterface(ClassName.get(classInterface))
                        .build()
        ).build().writeTo(mFiler);
    }

    private ParameterSpec buildParam() {
        // 参数需要实现的IApplication
        TypeElement genericParam = elementUtil.getTypeElement("com.ooftf.docking.api.IApplication");
        // 需要实现方法register的参数 void register(List<IApplication> list);List<IApplication> list

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(List.class),
                ClassName.get(genericParam)
        );

        // Build input param name.
        return ParameterSpec.builder(parameterizedTypeName, "applications").build();
    }

    /**
     * Verify AppShip meta
     *
     * @param element AppShip taw type
     * @return verify result
     */
    private boolean verify(Element element) {
        AppShip AppShip = element.getAnnotation(AppShip.class);
        // It must be implement the interface IApplication and marked with annotation AppShip.
        return null != AppShip && ((TypeElement) element).getInterfaces().contains(iApplication);
    }

    private void log(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Docking::" + message);
    }
}
