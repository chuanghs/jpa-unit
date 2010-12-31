package org.ormunit.entity;

import org.ormunit.exception.ORMEntityAccessException;
import org.ormunit.exception.ORMUnitAccessorException;
import org.ormunit.exception.ORMUnitInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.Collection;
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
            throw new ORMEntityAccessException(e);
        }
    }

    public PropertyDescriptor[] getProperties() {
        return descriptors.values().toArray(new PropertyDescriptor[descriptors.size()]);
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
            log.warn("no property: " + propertyName + " in class: " + clazz.getCanonicalName());
            return null;
        }
    }

    public void set(Object entity, String propertyName, Object value) {
        try {
            PropertyDescriptor pd = getPD(propertyName);
            if (pd == null) {
                log.warn("attribute: " + pd.getName() + " does not have corresponding property in class: " + clazz.getCanonicalName());
                return;
            }
            Method setter = pd.getWriteMethod();
            if (setter == null) {
                log.warn("there is no setter for property: " + pd.getName() + " of class: " + clazz.getCanonicalName());
                return;
            }

            setter.invoke(entity, value);
        } catch (Exception e) {
            throw new ORMEntityAccessException(e);
        }
    }

    public Object get(Object entity, String propertyName) {
        try {
            PropertyDescriptor pd = getPD(propertyName);
            if (pd == null) {
                log.warn("attribute: " + pd.getName() + " does not have corresponding property in class: " + clazz.getCanonicalName());
                return null;
            }
            Method getter = pd.getReadMethod();
            if (getter == null) {
                log.warn("there is no setter for property: " + pd.getName() + " of class: " + clazz.getCanonicalName());
                return null;
            }

            return getter.invoke(entity);
        } catch (Exception e) {
            throw new ORMEntityAccessException(e);
        }
    }

    public Class getCollectionParameterType(String propertyName) {
        PropertyDescriptor pd = getPD(propertyName);
        if (Collection.class.isAssignableFrom(pd.getPropertyType())) {
            Type genericReturnType = pd.getReadMethod().getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                Type type = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
                return extractClass(type);
            }
        } else
            throw new ORMUnitAccessorException("property: "+propertyName+" of class: "+getClass().getCanonicalName()+" is not Collection");
        return Object.class;
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
