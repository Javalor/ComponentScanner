package io.javalor.componentscanner;

import com.google.gson.JsonSerializer;

import java.io.Serializable;
import java.util.Map;

public interface AnnotationInfo extends JsonSerializer<AnnotationInfo> {

    ComponentInfo getAnnotationType();
    Map<String, AnnotationValueInfo> getAnnotationValues();
}
