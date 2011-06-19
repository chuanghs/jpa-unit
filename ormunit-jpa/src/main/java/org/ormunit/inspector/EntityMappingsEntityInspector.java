package org.ormunit.inspector;

import com.sun.java.xml.ns.persistence.orm.*;
import org.ormunit.BeanUtils;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitInstantiationException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.List;
/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 16.06.11
 * Time: 14:35
 */
public class EntityMappingsEntityInspector extends DelegatingEntityInspector {

    private EntityMappings entityMappings;

    private BeanUtils utils = new BeanUtils();

    public EntityMappingsEntityInspector(EntityMappings entityMappings, EntityInspector backupInspector) {
        super(backupInspector);
        this.entityMappings = entityMappings;
    }

    public String getSchemaName(Class<?> entityClass) {
        Entity entityEntry = getEntityEntry(entityClass);
        if (entityClass != null && entityEntry.getTable() != null) {
            entityEntry.getTable().getSchema();
        }
        return super.getSchemaName(entityClass);
    }

    public AccessType getAccessTypeOfClass(Class entityClass) {
        Entity requestedEntityEntry = getEntityEntry(entityClass);
        if (requestedEntityEntry != null && requestedEntityEntry.getAccess() != null) {
            return requestedEntityEntry.getAccess();
        }
        return super.getAccessTypeOfClass(entityClass);
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
        return super.getIdTypeOfEntityClass(entityClass);
    }

    public boolean isIdGenerated(Class<?> entityClass) {
        Id idEntry = getIdEntry(entityClass);
        if (idEntry != null)
            return idEntry.getGeneratedValue() != null;
        return super.isIdGenerated(entityClass);
    }

    public Class getIdClass(Class<?> entityClass) {
        Class<?> idTypeOfEntityClass = getIdTypeOfEntityClass(entityClass);
        if (idTypeOfEntityClass != null) {
            return idTypeOfEntityClass;
        } else {
            if (getAccessTypeOfClass(entityClass) == AccessType.FIELD) {
                Field idField = getIdField(entityClass);
                if (idField != null) {
                    return idField.getType();
                }
            } else {
                PropertyDescriptor idProperty = getIdProperty(entityClass);
                if (idProperty != null) {
                    return idProperty.getPropertyType();
                }
            }
        }
        return super.getIdClass(entityClass);
    }

    public PropertyDescriptor getIdProperty(Class<?> entityClass) {
        Id id = getIdEntry(entityClass);
        if (id != null) {
            return utils.getProperty(entityClass, id.getName());
        }
        return super.getIdProperty(entityClass);
    }

    public Field getIdField(Class<?> entityClass) {
        Id id = getIdEntry(entityClass);
        if (id != null) {
            return utils.getField(entityClass, id.getName());
        }
        return super.getIdField(entityClass);

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

}
