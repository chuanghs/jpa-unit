package org.ormunit.command;

import org.ormunit.entity.EntityAccessor;

import java.beans.IntrospectionException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 20:14
 */
public class EntityReference {

    private EntityAccessor entityAccessor;
    private String propertyName;
    private final Object id;

    public EntityReference(EntityAccessor entityAccessor, String propertyName, Object id) throws IntrospectionException {
        this.entityAccessor = entityAccessor;
        this.propertyName = propertyName;
        this.id = id;
    }

    public Class getPropertyClass() {
        return entityAccessor.getPropertyType(propertyName);
    }

    public Object getId() {
        return id;
    }

    public void set(Object entity, Object value) {
        try {
            entityAccessor.set(entity, propertyName, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "EntityReference{" +
                ", property=" + propertyName +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityReference that = (EntityReference) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (entityAccessor != null ? !entityAccessor.equals(that.entityAccessor) : that.entityAccessor != null)
            return false;
        if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entityAccessor != null ? entityAccessor.hashCode() : 0;
        result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
