package io.javalor.componentscanner.annotation;

import com.google.auto.service.AutoService;
import org.reflections.Reflections;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
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

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        note("Processor IncludedInComponentScanAnnotationProcessor");
        if (annotations.size() == 0) {
            note("Nothing to process");
            return false;
        }

        annotations.forEach(annotation-> note("Annotation "+annotation));

        final Set<String> elements = roundEnv.getElementsAnnotatedWithAny(getSupportedAnnotationClass())
                .stream()
                .peek(element -> {
                    note("Scanning "+String.join(".",
                            element.getEnclosingElement().asType().toString(),element.getSimpleName().toString()));
                })
                .filter(element -> (element.getKind() == ElementKind.CLASS) )
                .map(element -> element.getEnclosingElement().asType().toString()+"."+element.getSimpleName().toString())
                .collect(Collectors.toSet());

        try {
            FileObject resourceFile = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT,"","META-INF/io.javalor/component-list.lsv");
            note("Adding Component: "+resourceFile.getName());
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
                    .collect(Collectors.toUnmodifiableSet());

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private void note(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
