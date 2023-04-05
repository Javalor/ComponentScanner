package io.javalor.componentscanner;

import com.google.gson.*;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnnotationInfoImpl implements AnnotationInfo{

    ComponentInfo annotationType;
    Map<String, AnnotationValueInfo> annotationValue;

    public AnnotationInfoImpl(AnnotationMirror annotationMirror) {
        this.annotationType = new ComponentInfoImpl(annotationMirror.getAnnotationType().asElement());
        annotationValue = new LinkedHashMap<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            AnnotationValueInfo annotationValueInfo = new AnnotationValueInfoImpl(entry);
            annotationValue.put(annotationValueInfo.getValueName(), annotationValueInfo);
        }
    }

    @Override
    public ComponentInfo getAnnotationType() {
        return annotationType;
    }

    @Override
    public Map<String, AnnotationValueInfo> getAnnotationValues() {
        return annotationValue;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public JsonElement serialize(AnnotationInfo annotationInfo, Type type, JsonSerializationContext jsonSerializationContext) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("annotationType",gson.toJsonTree(annotationInfo.getAnnotationType()));
        jsonObject.add("annotationValueInfoMap",gson.toJsonTree(annotationInfo));
        return jsonObject;
    }
}
