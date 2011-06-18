package org.ormunit;

import com.sun.java.xml.ns.persistence.orm.*;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitInstantiationException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.List;

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

    private BeanUtils utils = new BeanUtils();

    public ORMFileEntityInspector(EntityMappings entityMappings, JPAEntityInspector backupInspector) {
        this.entityMappings = entityMappings;
        this.backupInspector = backupInspector;
    }

    public AccessType getAccessTypeOfClass(Class entityClass) {
        Entity requestedEntityEntry = getEntityEntry(entityClass);
        if (requestedEntityEntry != null && requestedEntityEntry.getAccess() != null) {
            return requestedEntityEntry.getAccess();
        }
        return backupInspector.getAccessTypeOfClass(entityClass);
    }

    private Entity getEntityEntry(Class entityClass) {
        List<Entity> entities = this.entityMappings.getEntity();
        if (entities != null) {
            for (Entity entity : entities) {
                if (entityClass.getCanonicalName().equals(entity.getClazz())) {
                    return entity;
                }
            }
        }
        return null;
    }

    public Class<?> getIdTypeOfEntityClass(Class<?> entityClass) {
        Entity requestedEntityEntry = getEntityEntry(entityClass);
        if (requestedEntityEntry != null && requestedEntityEntry.getIdClass() != null) {
            try {
                return Class.forName(requestedEntityEntry.getIdClass().getClazz());
            } catch (ClassNotFoundException e) {
                throw new ORMUnitInstantiationException(String.format("Cannot instantiate idclass: %s (based on orm file) of %s",
                        requestedEntityEntry.getIdClass().getClazz(),
                        entityClass.getCanonicalName()));
            }
        }
        return backupInspector.getIdTypeOfEntityClass(entityClass);
    }

    public boolean isIdGenerated(Class<?> entityClass) {
        Id idEntry = getIdEntry(entityClass);
        if (idEntry!=null)
            return idEntry.getGeneratedValue()!=null;
        return backupInspector.isIdGenerated(entityClass);
    }

    public Class getIdClass(Class<?> entityClass) {
        Class<?> idTypeOfEntityClass = getIdTypeOfEntityClass(entityClass);
        if (idTypeOfEntityClass!=null) {
            return idTypeOfEntityClass;
        } else {
            if (getAccessTypeOfClass(entityClass) == AccessType.FIELD) {
                Field idField = getIdField(entityClass);
                if (idField!=null) {
                    return idField.getType();
                }
            } else {
                PropertyDescriptor idProperty = getIdProperty(entityClass);
                if (idProperty!=null) {
                    return idProperty.getPropertyType();
                }
            }
        }
        return backupInspector.getIdClass(entityClass);
    }

    public PropertyDescriptor getIdProperty(Class<?> entityClass) {
        Id id = getIdEntry(entityClass);
        if (id != null) {
            return utils.getProperty(entityClass, id.getName());
        }
        return backupInspector.getIdProperty(entityClass);
    }

    private Id getIdEntry(Class<?> entityClass) {
        Entity entityEntry = getEntityEntry(entityClass);
        if (entityEntry != null) {
            Attributes attributes = entityEntry.getAttributes();
            if (attributes != null) {
                List<Id> ids = attributes.getId();
                if (ids != null) {
                    if (ids.size() == 1) {
                        return ids.iterator().next();
                    } else if (ids.size() > 1) {
                        throw new ORMUnitConfigurationException(String.format("Ambiguous id in ORM file for %s", entityClass.getCanonicalName()));
                    }
                }
            }
        }
        return null;
    }

    public Field getIdField(Class<?> entityClass) {
        Id id = getIdEntry(entityClass);
        if (id != null) {
            return utils.getField(entityClass, id.getName());
        }
        return backupInspector.getIdField(entityClass);

    }

}
