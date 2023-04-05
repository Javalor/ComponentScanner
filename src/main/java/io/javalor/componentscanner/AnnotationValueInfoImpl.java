package io.javalor.componentscanner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class AnnotationValueInfoImpl implements AnnotationValueInfo {

    private final TypeKind valueTypeKind;
    private final TypeMirror valueTypeMirror;
    private final String valueName;
    private final Object value;
    private final Optional<Object> defaultValue;

    public AnnotationValueInfoImpl(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> elementValueEntry) {

        this.valueTypeKind  = elementValueEntry.getKey().getReturnType().getKind();
        this.valueTypeMirror = elementValueEntry.getKey().getReturnType();

        this.valueName = elementValueEntry.getKey().getSimpleName().toString();

        this.value = elementValueEntry.getValue().getValue();
        this.defaultValue = Optional.ofNullable(elementValueEntry.getKey().getDefaultValue());
    }

    @Override
    public TypeKind getValueTypeKind() {
        return this.valueTypeKind;
    }

    @Override
    public TypeMirror getValueTypeMirror() {
        return this.valueTypeMirror;
    }

    @Override
    public String getValueName() {
        return this.valueName;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public JsonElement serialize(AnnotationValueInfo annotationValueInfo, Type type, JsonSerializationContext jsonSerializationContext) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("valueTypeKind",gson.toJsonTree(annotationInfo.getAnnotationType()));
        jsonObject.add("annotationValueInfoMap",gson.toJsonTree(annotationInfo));
        return jsonObject;
    }
}
