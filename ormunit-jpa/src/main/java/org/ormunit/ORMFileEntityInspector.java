package org.ormunit;

import com.sun.java.xml.ns.persistence.orm.AccessType;
import com.sun.java.xml.ns.persistence.orm.EntityMappings;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 18.06.11
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */
public class ORMFileEntityInspector implements JPAEntityInspector {

    private EntityMappings entityMappings;

    private JPAEntityInspector backupInspector;

    public ORMFileEntityInspector(EntityMappings entityMappings, JPAEntityInspector backupInspector) {
        this.entityMappings = entityMappings;
        this.backupInspector = backupInspector;
    }

    public AccessType getAccessTypeOfClass(Class entityClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class<?> getIdTypeOfClass(Class<?> entityClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isIdGenerated(Object entity, Object o, JPAORMProvider jpaormProvider) throws IntrospectionException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class getIdClassType(Class<?> entityClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyDescriptor getIdProperty(Class<?> entityClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Field getIdField(Class<?> entityClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
