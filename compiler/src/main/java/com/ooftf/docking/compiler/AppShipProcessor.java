package com.ooftf.docking.compiler;

import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;
import com.google.auto.service.AutoService;
import com.ooftf.docking.annotation.AppShip;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import static com.ooftf.docking.compiler.Consts.KEY_MODULE_NAME;

@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(ANNOTATION_TYPE_INTECEPTOR)
public class AppShipProcessor extends AbstractProcessor {
    private Map<Integer, Element> AppShips = new TreeMap<>();
    private Filer mFiler;       // File util, write class file into disk.
    private Elements elementUtil;
    private String moduleName = null;   // Module name, maybe its 'app' or others
    private TypeMirror iAppShip = null;

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

        mFiler = processingEnv.getFiler();                  // Generate class.
        elementUtil = processingEnv.getElementUtils();      // Get class meta.

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

        iAppShip = elementUtil.getTypeElement(Consts.IAPPLICATION).asType();

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
                if (verify(element)) {  // Check the AppShip meta

                    AppShip annotation = element.getAnnotation(AppShip.class);

                    Element lastAppShip = AppShips.get(annotation.priority());
                    if (null != lastAppShip) { // Added, throw exceptions
                        throw new IllegalArgumentException(
                                String.format(Locale.getDefault(), "More than one AppShips use same priority [%d], They are [%s] and [%s].",
                                        AppShip.priority(),
                                        lastAppShip.getSimpleName(),
                                        element.getSimpleName())
                        );
                    }

                    AppShips.put(AppShip.priority(), element);
                } else {

                }
            }

            // Interface of ARouter.
            TypeElement type_ITollgate = elementUtil.getTypeElement(IAppShip);
            TypeElement type_ITollgateGroup = elementUtil.getTypeElement(IAppShip_GROUP);

            /**
             *  Build input type, format as :
             *
             *  ```Map<Integer, Class<? extends ITollgate>>```
             */
            ParameterizedTypeName inputMapTypeOfTollgate = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(Integer.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(type_ITollgate))
                    )
            );

            // Build input param name.
            ParameterSpec tollgateParamSpec = ParameterSpec.builder(inputMapTypeOfTollgate, "AppShips").build();

            // Build method : 'loadInto'
            MethodSpec.Builder loadIntoMethodOfTollgateBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(tollgateParamSpec);

            // Generate
            if (null != AppShips && AppShips.size() > 0) {
                // Build method body
                for (Map.Entry<Integer, Element> entry : AppShips.entrySet()) {
                    loadIntoMethodOfTollgateBuilder.addStatement("AppShips.put(" + entry.getKey() + ", $T.class)", ClassName.get((TypeElement) entry.getValue()));
                }
            }

            // Write to disk(Write file even AppShips is empty.)
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(NAME_OF_AppShip + SEPARATOR + moduleName)
                            .addModifiers(PUBLIC)
                            .addJavadoc(WARNING_TIPS)
                            .addMethod(loadIntoMethodOfTollgateBuilder.build())
                            .addSuperinterface(ClassName.get(type_ITollgateGroup))
                            .build()
            ).build().writeTo(mFiler);

            logger.info(">>> AppShip group write over. <<<");
        }
    }

    /**
     * Verify inteceptor meta
     *
     * @param element AppShip taw type
     * @return verify result
     */
    private boolean verify(Element element) {
        AppShip AppShip = element.getAnnotation(AppShip.class);
        // It must be implement the interface IAppShip and marked with annotation AppShip.
        return null != AppShip && ((TypeElement) element).getInterfaces().contains(iAppShip);
    }
}
