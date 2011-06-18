package org.ormunit;

import com.sun.java.xml.ns.persistence.orm.AccessType;
import org.mockito.cglib.beans.BeanMap;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 18.06.11
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public interface JPAEntityInspector {

    AccessType getAccessTypeOfClass(Class entityClass);

    Class<?> getIdTypeOfClass(Class<?> entityClass);

    boolean isIdGenerated(Object entity, Object o, JPAORMProvider jpaormProvider) throws IntrospectionException;

    Class getIdClassType(Class<?> entityClass);

    PropertyDescriptor getIdProperty(Class<?> entityClass);

    Field getIdField(Class<?> entityClass);
}
