package io.javalor.componentscanner.annotation;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import io.javalor.componentscanner.AnnotationInfoImpl;
import io.javalor.componentscanner.ComponentInfo;
import io.javalor.componentscanner.ComponentInfoImpl;
import io.javalor.componentscanner.ComponentScanner;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService({IncludedInComponentScan.class})
public class IncludedInComponentScanAnnotationProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IncludedInComponentScanAnnotationProcessor.class);

    private static final Reflections R = new Reflections();
    private static final Set<Class<? extends Annotation>> ALL_ANNOTATIONS = R.getSubTypesOf(Annotation.class);
    private static final Set<Class<? extends Annotation>> SUPPORTED_ANNOTATIONS = getSupportedAnnotationClass();
    private Set<ComponentInfo> componentFound = new HashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        debug("Processor Start");
        debug("roundEnv.processingOver(): "+roundEnv.processingOver());

        if (roundEnv.processingOver()) {
            return processCreateResourceFile(annotations, roundEnv);
        }
        else {
            return processScan(annotations, roundEnv);
        }
    }

    public boolean processScan(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        debug("ProcessorScan Start");
        if (annotations.size() == 0) {
            debug("Nothing to process !!!");
            debug("annotations.size() == 0");
            return false;
        }

        Elements elementUtils = processingEnv.getElementUtils();

        annotations.forEach(annotation-> debug("annotations: "+annotation));

        final List<AnnotationMirror> annotationMirrorList =  new LinkedList<>();
        final Set<ComponentInfo> elementsFound = roundEnv.getElementsAnnotatedWithAny(getSupportedAnnotationClass()).stream()
                    .peek(element -> {
                        debug("Scanning "+String.join(".",
                                element.getEnclosingElement().asType().toString(),element.getSimpleName().toString()));
                        if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
                            List<? extends AnnotationMirror> myAnnotationMirrorList = element.getAnnotationMirrors();
                            annotationMirrorList.addAll(myAnnotationMirrorList);
                            debug(" * AnnotationMirror: "+myAnnotationMirrorList.stream()
                                    .map(am -> am.getAnnotationType().toString())
                                    .collect(Collectors.joining(", ")));
                        }
                        debug(" ** ALL Annotations: "+elementUtils.getAllAnnotationMirrors(element).stream()
                                .map(AnnotationInfoImpl::new).collect(Collectors.toSet()));

                     })
                .map(ComponentInfoImpl::new)
                .collect(Collectors.toSet());

        componentFound.addAll(elementsFound);

        return false;
    }

    public boolean processCreateResourceFile(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        debug("ProcessCreateResourceFile Start");

        final Set<String> elementJsonEncoded = componentFound
                .stream()
                .peek(cInfo -> debug("Got "+cInfo))
                .map(ComponentInfo::toString)
                .collect(Collectors.toSet());

        try {
            FileObject resourceFile = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT,"",
                            ComponentScanner.getResourceFilename());

            note("Resource file: "+resourceFile.getName());
            Writer writer = resourceFile.openWriter();
            writer.write(String.join("\n",elementJsonEncoded)+"\n");
            writer.close();

        } catch (IOException e) {
            error(e.toString());
        }

        return componentFound.size()>0;
    }



    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    protected static Set<Class<? extends Annotation>> getSupportedAnnotationClass() {

        Set<Class<? extends Annotation>> annotations = getAllAnnotationMirrorOf(IncludedInComponentScan.class);
        annotations.add(IncludedInComponentScan.class);

        return  annotations;

    }

    protected static Set<Class<? extends Annotation>> getAllAnnotationMirrorOf(Class<? extends Annotation> annotation) {
        logger.debug("Scanning for Annotation Mirror: "+annotation.getName());
        Set<Class<? extends Annotation>> myMirror = ALL_ANNOTATIONS.stream()
                .filter(a-> a.isAnnotationPresent(annotation))
                .peek(a->logger.debug(" Found Mirror "+a.getName()))
                .collect(Collectors.toSet());

        return Stream.concat(
                myMirror.stream(),
                myMirror.stream()
                    .map(IncludedInComponentScanAnnotationProcessor::getAllAnnotationMirrorOf)
                    .flatMap(Set::stream)

        ).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

            return SUPPORTED_ANNOTATIONS.stream().map(Class::getCanonicalName)
                    .peek(s-> debug("Support Annotation Class: "+s))
                    .collect(Collectors.toUnmodifiableSet());

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        note("SupportedSourceVersion: "+SourceVersion.latestSupported());
        return SourceVersion.latestSupported();
    }

    private void error(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
        logger.error(msg);
    }

    private void debug(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, msg);
        logger.debug(msg);
    }

    private void note(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        logger.info(msg);
    }
}
