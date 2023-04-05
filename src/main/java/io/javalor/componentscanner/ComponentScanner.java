package io.javalor.componentscanner;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.ElementKind;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentScanner {
    private static final Logger logger = LoggerFactory.getLogger(ComponentScanner.class);
    private static final String resourceFilename = "META-INF/io.javalor/component-scanner/component-list.lsv";

    public ComponentScanner() {
        logger.info("ComponentScanner: resource file = "+resourceFilename);
    }



    public static String getResourceFilename() {

        return resourceFilename;
    }

    public Set<Class<?>> getScannedClass() {
        return getScannedComponents().stream().map(info->getClass(info.getClassName())).filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<ComponentInfo> getScannedComponents() {
        try (
                BufferedInputStream bis = (BufferedInputStream) this.getClass().getClassLoader().getResourceAsStream(getResourceFilename());

        ) {

            if (bis == null)
                throw new FileNotFoundException("getResourceAsStream(\""+getResourceFilename()+"\") is 'null'.");

            logger.info("Reading "+getResourceFilename());
            BufferedReader componentReader = new BufferedReader(new InputStreamReader(bis));
            return componentReader.lines().peek(s-> logger.debug(" > "+s))
                    .map(ComponentInfoImpl::new)
                   .collect(Collectors.toUnmodifiableSet());
        }
        catch(Exception ex) {
            logger.error("Reading resource file Exception", ex);
            return Collections.emptySet();
        }
    }

    private Class<?> getClass(String className) {
        try {
            return  Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
