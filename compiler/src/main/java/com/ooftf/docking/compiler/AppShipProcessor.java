package com.ooftf.docking.compiler;

import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;
import com.google.auto.service.AutoService;
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

@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(ANNOTATION_TYPE_INTECEPTOR)
public class AppShipProcessor extends AbstractProcessor {
    private Map<Integer, Element> interceptors = new TreeMap<>();
    private Filer mFiler;       // File util, write class file into disk.
    private Elements elementUtil;
    private String moduleName = null;   // Module name, maybe its 'app' or others
    private TypeMirror iInterceptor = null;

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

        iInterceptor = elementUtil.getTypeElement(Consts.IINTERCEPTOR).asType();

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
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Interceptor.class);
            try {
                parseInterceptors(elements);
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
    private void parseInterceptors(Set<? extends Element> elements) throws IOException {
        if (CollectionUtils.isNotEmpty(elements)) {


            // Verify and cache, sort incidentally.
            for (Element element : elements) {
                if (verify(element)) {  // Check the interceptor meta

                    Interceptor interceptor = element.getAnnotation(Interceptor.class);

                    Element lastInterceptor = interceptors.get(interceptor.priority());
                    if (null != lastInterceptor) { // Added, throw exceptions
                        throw new IllegalArgumentException(
                                String.format(Locale.getDefault(), "More than one interceptors use same priority [%d], They are [%s] and [%s].",
                                        interceptor.priority(),
                                        lastInterceptor.getSimpleName(),
                                        element.getSimpleName())
                        );
                    }

                    interceptors.put(interceptor.priority(), element);
                } else {

                }
            }

            // Interface of ARouter.
            TypeElement type_ITollgate = elementUtil.getTypeElement(IINTERCEPTOR);
            TypeElement type_ITollgateGroup = elementUtil.getTypeElement(IINTERCEPTOR_GROUP);

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
            ParameterSpec tollgateParamSpec = ParameterSpec.builder(inputMapTypeOfTollgate, "interceptors").build();

            // Build method : 'loadInto'
            MethodSpec.Builder loadIntoMethodOfTollgateBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(tollgateParamSpec);

            // Generate
            if (null != interceptors && interceptors.size() > 0) {
                // Build method body
                for (Map.Entry<Integer, Element> entry : interceptors.entrySet()) {
                    loadIntoMethodOfTollgateBuilder.addStatement("interceptors.put(" + entry.getKey() + ", $T.class)", ClassName.get((TypeElement) entry.getValue()));
                }
            }

            // Write to disk(Write file even interceptors is empty.)
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(NAME_OF_INTERCEPTOR + SEPARATOR + moduleName)
                            .addModifiers(PUBLIC)
                            .addJavadoc(WARNING_TIPS)
                            .addMethod(loadIntoMethodOfTollgateBuilder.build())
                            .addSuperinterface(ClassName.get(type_ITollgateGroup))
                            .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Interceptor group write over. <<<");
        }
    }

    /**
     * Verify inteceptor meta
     *
     * @param element Interceptor taw type
     * @return verify result
     */
    private boolean verify(Element element) {
        Interceptor interceptor = element.getAnnotation(Interceptor.class);
        // It must be implement the interface IInterceptor and marked with annotation Interceptor.
        return null != interceptor && ((TypeElement) element).getInterfaces().contains(iInterceptor);
    }
}
