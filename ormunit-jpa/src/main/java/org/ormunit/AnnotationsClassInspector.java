package org.ormunit;


import com.sun.java.xml.ns.persistence.orm.AccessType;

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
public class AnnotationsClassInspector {

    private BeanUtils utils = new BeanUtils();


    AccessType getAccessType(Class entityClass) {
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

    public Object getId(Object entity) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        IdClass idClass = entity.getClass().getAnnotation(IdClass.class);
        Object result = null;
        if (idClass != null) {
            result = idClass.value().newInstance();
            if (getAccessType(entity.getClass()) == AccessType.PROPERTY) {
                result = idClass.value().newInstance();
                utils.copyPropertyValues(entity, result);
            } else {
                result = idClass.value().newInstance();
                utils.copyFieldValues(entity, result);
            }
        } else {
            if (getAccessType(entity.getClass()) == AccessType.PROPERTY) {
                Set<PropertyDescriptor> idProperties = utils.getPropertiesAnnotatedWith(entity.getClass(), Id.class, EmbeddedId.class);
                result = idProperties.iterator().next().getReadMethod().invoke(entity);
            } else {
                Set<Field> idFields = utils.getFieldsAnnotatedWith(entity.getClass(), Id.class, EmbeddedId.class);
                Field next = idFields.iterator().next();
                next.setAccessible(true);
                result = next.get(entity);
            }
        }
        return result;
    }

    Class<?> getIdClass2(Class<?> entityClass) {
        Class<?> result = null;
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            result = idClass.value();
        } else {
            if (getAccessType(entityClass) == AccessType.PROPERTY) {
                return utils.getPropertiesAnnotatedWith(entityClass, Id.class, EmbeddedId.class).iterator().next().getPropertyType();
            } else if (getAccessType(entityClass) == AccessType.FIELD) {
                return utils.getFieldsAnnotatedWith(entityClass, Id.class, EmbeddedId.class).iterator().next().getType();
            }
        }
        return result;
    }

    boolean isIdGenerated(Object entity, Object o, JPAORMProvider jpaormProvider) throws IntrospectionException {
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

    void setId_Annotations(Object entity, Object id) throws IllegalAccessException, InvocationTargetException {
        if (entity.getClass().getAnnotation(IdClass.class) != null) {
            if (getAccessType(entity.getClass()) == AccessType.PROPERTY)
                utils.copyPropertyValues(id, entity);
            else if (getAccessType(entity.getClass()) == AccessType.FIELD)
                utils.copyFieldValues(id, entity);
        } else {
            if (getAccessType(entity.getClass()) == AccessType.PROPERTY) {
                utils.getPropertiesAnnotatedWith(entity.getClass(), Id.class, EmbeddedId.class).iterator().next().getWriteMethod().invoke(entity, id);
            } else if (getAccessType(entity.getClass()) == AccessType.FIELD) {
                Field next = utils.getFieldsAnnotatedWith(entity.getClass(), Id.class, EmbeddedId.class).iterator().next();
                next.setAccessible(true);
                next.set(entity, id);
            }
        }
    }
}
