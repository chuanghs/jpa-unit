package org.ormunit;

import com.sun.java.xml.ns.persistence.orm.AccessType;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 18.06.11
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public interface JPAEntityInspector {

    AccessType getAccessTypeOfClass(Class entityClass);

    Class<?> getIdTypeOfEntityClass(Class<?> entityClass);

    PropertyDescriptor getIdProperty(Class<?> entityClass);

    Field getIdField(Class<?> entityClass);

    boolean isIdGenerated(Class<?> entity);

    Class getIdClass(Class<?> entityClass);
}
