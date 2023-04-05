package io.javalor.componentscanner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JustTetsting({"I", "am", "an", "annotation"})
public @interface IncludedInComponentScan {

}
