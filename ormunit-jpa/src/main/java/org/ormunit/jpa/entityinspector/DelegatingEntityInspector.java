package org.ormunit.jpa.entityinspector;

import com.sun.java.xml.ns.persistence.orm.AccessType;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

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


    public String getSchemaName(Class<?> entityClass) {
        return delegateTo.getSchemaName(entityClass);
    }

    public AccessType getAccessTypeOfClass(Class entityClass) {
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
