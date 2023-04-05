package io.javalor.componentscanner;

import com.google.gson.Gson;
import io.javalor.componentscanner.annotation.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ComponentInfoImpl implements ComponentInfo {

    private Set<Modifier> modifiers;
    private ElementKind elementKind;
    private String className;
    private final List<ComponentInfo> annotationList = new LinkedList<>();

    private static final long serialVersionUID = ("1.0.0"+ComponentInfo.class.getCanonicalName()).hashCode();

    private static final Logger logger = LoggerFactory.getLogger(ComponentInfoImpl.class);
    private static final ComponentInfo NULL = new ComponentInfoImpl(Null.class);

    public ComponentInfoImpl() {
        this(NULL);
    }

    public ComponentInfoImpl(ComponentInfo componentInfo) {
        this.modifiers = Set.copyOf(componentInfo.getModifiers());
        this.elementKind = componentInfo.getElementKind();
        this.className = componentInfo.getClassName();

    }

    public ComponentInfoImpl(Class<?> cls) {
        this.modifiers = extractModifierSetFromClass(cls);
        this.elementKind = extractElementKindFromClass(cls);
        this.className = cls.getCanonicalName();

    }

    public ComponentInfoImpl(String json) {
        this(NULL);
        Gson gson = new Gson();
        ComponentInfo o = gson.fromJson(json, ComponentInfoImpl.class);
        this.modifiers = o.getModifiers();
        this.elementKind = o.getElementKind();
        this.className = o.getClassName();

    }

    public ComponentInfoImpl(Element element) {
        this.modifiers = element.getModifiers();
        this.elementKind = element.getKind();
        this.className = String.join(".",
                            element.getEnclosingElement().asType().toString(),
                            element.getSimpleName().toString())
                        .replaceFirst("^\\.","");
    }

    private static ElementKind extractElementKindFromClass(Class<?> cls) {
        Map<ElementKind, Predicate<Class<?>>> kindTester = Map.of(
                ElementKind.ANNOTATION_TYPE, Class::isAnnotation,
                ElementKind.INTERFACE, Class::isInterface,
                ElementKind.ENUM, Class::isEnum
        );

        for (ElementKind kind : kindTester.keySet()) {
            if (kindTester.get(kind).test(cls))
                return kind;
        }
        return ElementKind.CLASS;
    }

    private static Set<Modifier> extractModifierSetFromClass(Class<?> cls) {
        Map<Modifier, Predicate<Integer>> modifierTester = Map.of(
                Modifier.PUBLIC, java.lang.reflect.Modifier::isPublic,
                Modifier.PROTECTED, java.lang.reflect.Modifier::isProtected,
                Modifier.PRIVATE, java.lang.reflect.Modifier::isPrivate,
                Modifier.ABSTRACT, java.lang.reflect.Modifier::isAbstract,
                Modifier.FINAL, java.lang.reflect.Modifier::isFinal,
                Modifier.SYNCHRONIZED, java.lang.reflect.Modifier::isSynchronized,
                Modifier.STRICTFP, java.lang.reflect.Modifier::isStrict
        );
        logger.debug("Start extractModifierSetFromClass("+cls.getCanonicalName()+")");
        int mod = cls.getModifiers();
        return Objects.requireNonNullElse(
                modifierTester.keySet().stream()
              //  .peek(m-> logger.debug("Before filtering Modifier."+m))
                .filter(m-> modifierTester.get(m).test(mod))
              //  .peek(m->logger.debug("GOT Modifier."+m.name()))
                .collect(Collectors.toUnmodifiableSet())
        , Collections.emptySet()
        );
    }

    @Override
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public ElementKind getElementKind() {
        return elementKind;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentInfoImpl that = (ComponentInfoImpl) o;

        if (getElementKind() != that.getElementKind()) return false;
        if (!getClassName().equals(that.getClassName())) return false;
        return getModifiers().equals(that.getModifiers());
    }

    @Override
    public int hashCode() {
        int result = getElementKind().hashCode();
        result = 31 * result + getClassName().hashCode();
        result = 31 * result + getModifiers().hashCode();
        return result;
    }


}
