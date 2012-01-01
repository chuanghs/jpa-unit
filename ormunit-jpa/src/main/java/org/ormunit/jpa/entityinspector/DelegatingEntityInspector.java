package org.ormunit.jpa.entityinspector;

import org.ormunit.ORMProviderAdapter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 19.06.11
 * Time: 09:10
 */
public class DelegatingEntityInspector implements EntityInspector {

    private EntityInspector delegateTo;

    public DelegatingEntityInspector(EntityInspector delegateTo) {
        this.delegateTo = delegateTo;
    }


    public Set<String> getSchemaNames(Class<?> entityClass) {
        return delegateTo.getSchemaNames(entityClass);
    }

    public ORMProviderAdapter.AccessType getAccessTypeOfClass(Class entityClass) {
        return delegateTo.getAccessTypeOfClass(entityClass);
    }

    public Class<?> getIdType(Class<?> entityClass) {
        return delegateTo.getIdType(entityClass);
    }

    public PropertyDescriptor getIdProperty(Class<?> entityClass) {
        return delegateTo.getIdProperty(entityClass);
    }

    public Field getIdField(Class<?> entityClass) {
        return delegateTo.getIdField(entityClass);
    }

    public boolean isIdGenerated(Class<?> entityClass) {
        return delegateTo.isIdGenerated(entityClass);
    }

    public Class getIdClassValue(Class<?> entityClass) {
        return delegateTo.getIdClassValue(entityClass);
    }
}
