package io.javalor.componentscanner.annotation;

import com.google.auto.service.AutoService;
import io.javalor.componentscanner.ComponentScanner;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService({IncludedInComponentScan.class})
public class IncludedInComponentScanAnnotationProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IncludedInComponentScanAnnotationProcessor.class);
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        note("Processor Start");
        if (annotations.size() == 0) {
            note("Nothing to process");
            debug("annotations.size() == 0");
            return false;
        }

        annotations.forEach(annotation-> debug("annotations: "+annotation));

        final Set<String> elements = roundEnv.getElementsAnnotatedWithAny(getSupportedAnnotationClass())
                .stream()
                .peek(element -> {
                    debug("Scanning "+String.join(".",
                            element.getEnclosingElement().asType().toString(),element.getSimpleName().toString()));
                })
                .map(element -> element.getKind()+","+element.getEnclosingElement().asType().toString()+"."+element.getSimpleName().toString())
                .collect(Collectors.toSet());

        try {
            FileObject resourceFile = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT,"",
                            ComponentScanner.getResourceFilename());

            note("Resource file: "+resourceFile.getName());
            Writer writer = resourceFile.openWriter();
            writer.write(String.join("\n",elements)+"\n");
            writer.close();

        } catch (IOException e) {
            error(e.toString());

        }

        return false;
    }
    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    protected static Set<Class<? extends Annotation>> getSupportedAnnotationClass() {

        Reflections reflections = new Reflections();

        return
                Stream.concat(reflections.getSubTypesOf(Annotation.class).stream()
                        .filter(c->c.isAnnotationPresent(IncludedInComponentScan.class)),
                        Stream.of(IncludedInComponentScan.class))
                        .collect(Collectors.toSet());

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

            return getSupportedAnnotationClass().stream().map(Class::getCanonicalName)
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
