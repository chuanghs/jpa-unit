package org.ormunit.command;

import org.ormunit.ORMProvider;

import javax.persistence.Id;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:43
 * To change this template use File | Settings | File Templates.
 */
public class JPAORMProvider implements ORMProvider {
    public Class<?> getIdType(Class<?> propertyType) {
        Class type = propertyType;
        do {
            for (Field f : type.getDeclaredFields()) {
                if (f.getAnnotation(Id.class) != null)
                    return f.getType();
            }
        } while ((type = type.getSuperclass()) != null);


        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(propertyType).getPropertyDescriptors()) {
                if (pd.getReadMethod() != null) {
                    if (pd.getReadMethod().getAnnotation(Id.class) != null) {
                        return pd.getPropertyType();
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
