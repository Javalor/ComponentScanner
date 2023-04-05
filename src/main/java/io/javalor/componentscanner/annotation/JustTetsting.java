package io.javalor.componentscanner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JustTetsting {
    int primtiveInt() default 0;
    String stringValue() default "hello";
    String[] value() default "hi";
}
