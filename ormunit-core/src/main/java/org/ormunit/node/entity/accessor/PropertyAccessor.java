package org.ormunit.node.entity.accessor;

import org.ormunit.exception.AccessorException;
import org.ormunit.exception.EntityAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 21.12.10
 * Time: 21:15
 */
public class PropertyAccessor extends AEntityAccessor {

    private static final Logger log = LoggerFactory.getLogger(PropertyAccessor.class);

    private Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();
    private Class<?> clazz;

    public PropertyAccessor(Class<?> clazz) {
        this.clazz = clazz;
        try {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            for (PropertyDescriptor pd : propertyDescriptors) {
                descriptors.put(pd.getName(), pd);
            }
        } catch (IntrospectionException e) {
            throw new EntityAccessException(e);
        }
    }

    public PropertyDescriptor getPD(String propertyName) {
        return descriptors.get(propertyName);
    }

    public Class getEntityClass() {
        return this.clazz;
    }

    public Class getType(String propertyName) {
        PropertyDescriptor propertyDescriptor = descriptors.get(propertyName);
        if (propertyDescriptor != null)
            return propertyDescriptor.getPropertyType();
        else {
            throw new AccessorException(String.format("No property: %s in class: %s",propertyName, clazz.getCanonicalName()));

        }
    }

    public void set(Object entity, String propertyName, Object value) {
        try {
            PropertyDescriptor pd = getPD(propertyName);
            if (pd == null) {
                throw new EntityAccessException(String.format("attribute: %s  does not have corresponding property in class: %s", propertyName,clazz.getCanonicalName()));
            }
            Method setter = pd.getWriteMethod();
            if (setter == null) {
                throw new EntityAccessException(String.format("there is no setter for property: %s of class: %s", pd.getName(), clazz.getCanonicalName()));
            }

            setter.invoke(entity, value);
        } catch (Exception e) {
            throw new EntityAccessException(e);
        }
    }

    public Object get(Object entity, String propertyName) {
        try {
            PropertyDescriptor pd = getPD(propertyName);
            if (pd == null) {
                throw new EntityAccessException(String.format("attribute: %s  does not have corresponding property in class: %s", propertyName, clazz.getCanonicalName()));
            }
            Method getter = pd.getReadMethod();
            if (getter == null) {
                throw new EntityAccessException(String.format("there is no getter for property: %s of class: %s", pd.getName(), clazz.getCanonicalName()));
            }

            return getter.invoke(entity);
        } catch (Exception e) {
            throw new EntityAccessException(e);
        }
    }

    public Class getCollectionParameterType(String propertyName) {
        PropertyDescriptor field = getPD(propertyName);
        return getCollectionParameterType(propertyName,
                (ParameterizedType) field.getReadMethod().getGenericReturnType());
    }


    public Class[] getMapParameterTypes(String propertyName) {
        PropertyDescriptor field = getPD(propertyName);
        return getMapParameterTypes(propertyName,
                (ParameterizedType) field.getReadMethod().getGenericReturnType());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyAccessor that = (PropertyAccessor) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return clazz != null ? clazz.hashCode() : 0;
    }

}
