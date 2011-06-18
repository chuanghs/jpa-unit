package org.ormunit;


import com.sun.java.xml.ns.persistence.orm.AccessType;
import org.ormunit.exception.ORMUnitConfigurationException;

import javax.persistence.EmbeddedId;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 16.06.11
 * Time: 20:55
 */
public class AnnotationsEntityInspector implements JPAEntityInspector {

    private BeanUtils utils = new BeanUtils();


    public AccessType getAccessTypeOfClass(Class entityClass) {
        Class clazz = entityClass;
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return AccessType.PROPERTY;
            }
            clazz = clazz.getSuperclass();
        }
        clazz = entityClass;
        while (clazz != null) {
            for (Field m : clazz.getDeclaredFields()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return AccessType.FIELD;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }



    public Field getIdField(Class<?> entityClass) {
        Set<Field> annotatedFields = utils.getFieldsAnnotatedWith(entityClass, Id.class, EmbeddedId.class);
        if (annotatedFields.size() == 0) {
            throw new ORMUnitConfigurationException("");
        } else if (annotatedFields.size() == 1) {
            return annotatedFields.iterator().next();
        } else {
            throw new RuntimeException();
        }
    }

    public PropertyDescriptor getIdProperty(Class<?> entityClass) {
        Set<PropertyDescriptor> annotatedProperties = utils.getPropertiesAnnotatedWith(entityClass, Id.class, EmbeddedId.class);
        if (annotatedProperties.size() == 0) {
            throw new RuntimeException();
        } else if (annotatedProperties.size() == 1) {
            return annotatedProperties.iterator().next();
        } else {
            throw new RuntimeException();
        }
    }

    public Class getIdClassType(Class<?> entityClass) {
        IdClass idClassAnnotation = entityClass.getAnnotation(IdClass.class);
        if (idClassAnnotation != null)
            return idClassAnnotation.value();
        return null;
    }

    public Class<?> getIdTypeOfClass(Class<?> entityClass) {
        Class<?> result = null;
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            result = idClass.value();
        } else {
            if (getAccessTypeOfClass(entityClass) == AccessType.PROPERTY) {
                return utils.getPropertiesAnnotatedWith(entityClass, Id.class, EmbeddedId.class).iterator().next().getPropertyType();
            } else if (getAccessTypeOfClass(entityClass) == AccessType.FIELD) {
                return utils.getFieldsAnnotatedWith(entityClass, Id.class, EmbeddedId.class).iterator().next().getType();
            }
        }
        return result;
    }

    public boolean isIdGenerated(Object entity, Object o, JPAORMProvider jpaormProvider) throws IntrospectionException {
        if (jpaormProvider.isPropertyAccessed(entity.getClass())) {
            for (Field field : utils.getFieldsAnnotatedWith(entity.getClass(), Id.class)) {
                if (field.getAnnotation(GeneratedValue.class) != null) {
                    return true;
                }
            }
        } else if (jpaormProvider.isFieldAccessed(entity.getClass())) {
            for (PropertyDescriptor pd : utils.getPropertiesAnnotatedWith(entity.getClass(), Id.class)) {
                if (pd.getReadMethod().getAnnotation(GeneratedValue.class) != null) {
                    return true;
                }
            }
        }
        return false;
    }


}
