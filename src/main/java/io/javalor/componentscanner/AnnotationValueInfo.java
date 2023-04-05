package io.javalor.componentscanner;

import com.google.gson.JsonSerializer;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Optional;

import static javax.lang.model.type.TypeKind.*;

public interface AnnotationValueInfo extends JsonSerializer<AnnotationValueInfo> {
    Map<TypeKind, Class<?>> TYPE_BOXING_MAP = Map.of(
            BOOLEAN, Boolean.class,
            BYTE, Byte.class,
            CHAR, Character.class,
            DOUBLE, Double.class,
            FLOAT, Float.class,
            INT, Integer.class,
            LONG, Long.class,
            SHORT, Short.class
            );
    TypeKind getValueTypeKind();
    TypeMirror getValueTypeMirror();
    default String getValueType() {
       return getValueTypeMirror().toString().replace("[]","");
    }
    String getValueName();
    Object getValue();
    Optional<Object> getDefaultValue();


}
