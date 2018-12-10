package com.ooftf.docking.compiler;

import com.google.auto.service.AutoService;
import com.ooftf.docking.annotation.Application;
import com.ooftf.docking.annotation.Consts;
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

import static com.ooftf.docking.annotation.Consts.KEY_MODULE_NAME;
import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
@SupportedOptions(Consts.KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({Consts.APPLICATION_ANNOTATION_NAME})
public class ApplicationProcessor extends AbstractProcessor {
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
        messager = processingEnv.getMessager();
        log("init");
        // Generate class.  用于生成java文件
        mFiler = processingEnv.getFiler();
        // Get class meta.
        elementUtil = processingEnv.getElementUtils();

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");

        } else {
            throw new RuntimeException("Docking::Compiler >>> No module name, for more information, look at gradle log.");
        }
        //添加注解@AppShip 需要实现的接口
        iApplication = elementUtil.getTypeElement(Consts.IAPPLICATION_INTERFACE_NAME).asType();

    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log("process" + annotations);
        if (CollectionUtils.isNotEmpty(annotations)) {
            log("isNotEmpty");
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Application.class);
            log("isNotEmpty" + elements.toString());
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
        log("parseAppShips");
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
        log("buildJavaClass");
        // 生成类需要实现的接口
        TypeElement classInterface = elementUtil.getTypeElement(Consts.GEN_CLASS_INTERFACE_NAME);
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
                loadIntoMethodOfTollgateBuilder.addStatement("applications.add(new $T());", ClassName.get((TypeElement) e));
            }
        }

        // Write to disk(Write file even AppShips is empty.)
        JavaFile.builder(Consts.REGISTER_PACKAGE_NAME,
                TypeSpec.classBuilder(Consts.PROJECT + Consts.SEPARATOR + Consts.SUFFIX_APPLICATION + Consts.SEPARATOR + moduleName)
                        .addModifiers(PUBLIC)
                        .addJavadoc(Consts.WARNING_TIPS)
                        .addMethod(loadIntoMethodOfTollgateBuilder.build())
                        .addSuperinterface(ClassName.get(classInterface))
                        .build()
        ).build().writeTo(mFiler);
    }

    private ParameterSpec buildParam() {
        log("buildParam");
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
        log("verify");
        Application AppShip = element.getAnnotation(Application.class);
        // It must be implement the interface IApplication and marked with annotation AppShip.
        return null != AppShip && ((TypeElement) element).getInterfaces().contains(iApplication);
    }

    private void log(String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, "Docking--compiler::" + message);
    }
}
