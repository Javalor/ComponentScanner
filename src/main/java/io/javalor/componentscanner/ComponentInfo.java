package io.javalor.componentscanner;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.io.Serializable;

import java.util.Set;

public interface ComponentInfo extends Serializable {

    Set<Modifier> getModifiers();
    String getClassName();
    ElementKind getElementKind();

}
