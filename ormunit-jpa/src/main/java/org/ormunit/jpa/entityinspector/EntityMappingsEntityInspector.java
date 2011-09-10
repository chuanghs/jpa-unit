package org.ormunit.jpa.entityinspector;

import com.sun.java.xml.ns.persistence.orm.*;
import org.ormunit.BeanUtils;
import org.ormunit.ORMProviderAdapter;
import org.ormunit.exception.ConfigurationException;
import org.ormunit.exception.EntityInstantiationException;

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
        if (entityEntry != null && entityEntry.getTable() != null) {
            return entityEntry.getTable().getSchema();
        }
        return super.getSchemaName(entityClass);
    }

    public ORMProviderAdapter.AccessType getAccessTypeOfClass(Class entityClass) {
        Entity entityEntry = getEntityEntry(entityClass);
        if (entityEntry != null && entityEntry.getAccess() != null) {
            switch (entityEntry.getAccess()) {
                case FIELD:
                    return ORMProviderAdapter.AccessType.Field;
                case PROPERTY:
                    return ORMProviderAdapter.AccessType.Property;
                default:
                    throw new IllegalStateException("New access type was intruduces? This version does not support it " + entityEntry.getAccess());
            }
        }
        return super.getAccessTypeOfClass(entityClass);
    }

    public Class<?> getIdType(Class<?> entityClass) {
        Class idClass = getIdClassValue(entityClass);
        if (idClass != null) {
            return idClass;
        }
        if (getAccessTypeOfClass(entityClass) == ORMProviderAdapter.AccessType.Field) {
            Field idField = getIdField(entityClass);
            if (idField != null)
                return idField.getType();
        } else if (getAccessTypeOfClass(entityClass) == ORMProviderAdapter.AccessType.Property) {
            PropertyDescriptor idProperty = getIdProperty(entityClass);
            if (idProperty != null)
                return idProperty.getPropertyType();
        }

        return super.getIdType(entityClass);
    }


    public boolean isIdGenerated(Class<?> entityClass) {
        Id idEntry = getIdEntry(entityClass);
        if (idEntry != null)
            return idEntry.getGeneratedValue() != null;
        return super.isIdGenerated(entityClass);
    }

    public Class getIdClassValue(Class<?> entityClass) {
        IdClass idClassEntry = getIdClassEntry(entityClass);
        if (idClassEntry != null) {
            return instantiateIdTypeOf(entityClass, idClassEntry.getClazz());
        }
        return super.getIdClassValue(entityClass);
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

    private Class<?> instantiateIdTypeOf(Class<?> entityClass, String idTypeName) {
        try {
            return Class.forName(idTypeName);
        } catch (ClassNotFoundException e) {
            throw new EntityInstantiationException(String.format("Cannot instantiate idclass: %s (based on orm file) of %s",
                    idTypeName,
                    entityClass.getCanonicalName()));
        }
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

    private IdClass getIdClassEntry(Class<?> entityClass) {
        Entity entityEntry = getEntityEntry(entityClass);
        if (entityEntry != null) {
            return entityEntry.getIdClass();
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
                        throw new ConfigurationException(String.format("Ambiguous id in ORM file for %s", entityClass.getCanonicalName()));
                    }
                }
            }
        }
        return null;
    }

}
