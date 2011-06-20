package org.ormunit.jpa.entityinspector;

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
public interface EntityInspector {

    String getSchemaName(Class<?> entityClass);

    AccessType getAccessTypeOfClass(Class entityClass);

    Class<?> getIdType(Class<?> entityClass);

    PropertyDescriptor getIdProperty(Class<?> entityClass);

    Field getIdField(Class<?> entityClass);

    boolean isIdGenerated(Class<?> entityClass);

    Class getIdClassValue(Class<?> entityClass);
}
