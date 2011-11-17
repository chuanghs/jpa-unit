package org.ormunit.jpa.entityinspector;

import org.ormunit.ORMProviderAdapter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 18.06.11
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public interface EntityInspector {

    Set<String> getSchemaNames(Class<?> entityClass);

    ORMProviderAdapter.AccessType getAccessTypeOfClass(Class entityClass);

    Class<?> getIdType(Class<?> entityClass);

    PropertyDescriptor getIdProperty(Class<?> entityClass);

    Field getIdField(Class<?> entityClass);

    boolean isIdGenerated(Class<?> entityClass);

    Class getIdClassValue(Class<?> entityClass);
}
