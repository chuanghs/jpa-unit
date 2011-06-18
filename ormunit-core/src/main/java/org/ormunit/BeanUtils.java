package org.ormunit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 24.04.11
 * Time: 20:43
 */
public class BeanUtils {

    private static final Logger log = LoggerFactory.getLogger(BeanUtils.class);

    public Field getField(Class<?> pojoClass, String fieldName) {
        if (pojoClass == null)
            return null;

        for (Field f : pojoClass.getDeclaredFields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        return getField(pojoClass.getSuperclass(), fieldName);
    }

    public PropertyDescriptor getProperty(Class<?> pojoClass, String propertyName) {
        if (pojoClass == null)
            return null;

        try {
            for (PropertyDescriptor f : Introspector.getBeanInfo(pojoClass).getPropertyDescriptors()) {
                if (f.getName().equals(propertyName)) {
                    return f;
                }
            }
        } catch (IntrospectionException e) {
            log.warn(String.format("Cannot list %s properties", pojoClass.getCanonicalName()), e);
        }


        return getProperty(pojoClass.getSuperclass(), propertyName);
    }

    public Object copyFieldValues(Object source, Object target) {
        copyFieldValues(source, target, target.getClass());
        return target;
    }

    public void copyFieldValues(Object source, Object target, Class<?> aClass) {
        if (!aClass.isInstance(target))
            throw new RuntimeException(String.format("target (%s) is not subclass of specified class: (%s)", target.getClass().getCanonicalName(), aClass.getCanonicalName()));

        if (aClass == null)
            return;

        for (Field targetField : aClass.getDeclaredFields()) {
            Field sourceField = getField(source.getClass(), targetField.getName());
            if (sourceField != null) {
                sourceField.setAccessible(true);
                Object value = null;
                try {
                    value = sourceField.get(source);
                    targetField.setAccessible(true);
                    targetField.set(target, value);
                } catch (IllegalAccessException e) {
                    log.error("cannot copy value of property: " + targetField.getName(), e);
                }
            }
        }
    }

    public Object copyPropertyValues(Object source, Object target) {
        copyPropertyValues(source, target, target.getClass());
        return target;
    }

    public void copyPropertyValues(Object source, Object target, Class<?> targetClass) {
        if (!targetClass.isInstance(target))
            throw new RuntimeException(String.format("target (%s) is not subclass of specified class: (%s)", target.getClass().getCanonicalName(), targetClass.getCanonicalName()));

        if (targetClass == null)
            return;

        try {
            for (PropertyDescriptor targetProperty : Introspector.getBeanInfo(targetClass).getPropertyDescriptors()) {
                if (targetProperty.getName().equals("class") || targetProperty.getName().equals("hashCode")) {
                    continue;
                }

                PropertyDescriptor sourceProperty = getProperty(source.getClass(), targetProperty.getName());
                if (sourceProperty.getReadMethod() == null) {
                    log.warn(String.format("There is not getter of property %s in class %s", targetProperty.getName(), source.getClass().getCanonicalName()));
                } else if (targetProperty.getWriteMethod() == null) {
                    log.warn(String.format("There is not setter of property %s in class %s", targetProperty.getName(), target.getClass().getCanonicalName()));
                } else {
                    try {

                        Object value = sourceProperty.getReadMethod().invoke(source);
                        targetProperty.getWriteMethod().invoke(target, value);
                    } catch (Exception e) {
                        log.warn(String.format("when copying value: %s", targetProperty.getName()));
                    }
                }
            }
        } catch (IntrospectionException e) {
            log.warn(String.format("Cannot list %s properties", targetClass.getCanonicalName()), e);
        }
    }

    public Set<Field> getFieldsAnnotatedWith(Class<?> clazz, Class<? extends Annotation>... annotations) {
        Set<Field> result = new HashSet<Field>();
        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                for (Class<? extends Annotation> annotation : annotations)
                    if (f.getAnnotation(annotation) != null) {
                        result.add(f);
                    }
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }

    public Set<PropertyDescriptor> getPropertiesAnnotatedWith(Class<?> clazz, Class<? extends Annotation>... annotations) {
        Set<PropertyDescriptor> result = new HashSet<PropertyDescriptor>();
        try {
            while (clazz != null) {
                for (PropertyDescriptor f : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                    if (f.getReadMethod()==null)
                        continue;
                    for (Class<? extends Annotation> annotation : annotations)
                        if (f.getReadMethod().getAnnotation(annotation) != null) {
                            result.add(f);
                        }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(String.format("Cannot list %s properties", clazz.getCanonicalName()), e);
        }
        return result;
    }
}
