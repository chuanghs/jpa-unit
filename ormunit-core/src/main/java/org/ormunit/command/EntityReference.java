package org.ormunit.command;

import org.ormunit.ORMUnitIntrospector;

import java.beans.IntrospectionException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 20:14
 */
public class EntityReference {

    private ORMUnitIntrospector introspector;
    private String propertyName;
    private final Object id;

    public EntityReference(ORMUnitIntrospector introspector, String propertyName, Object id) throws IntrospectionException {
        this.introspector = introspector;
        this.propertyName = propertyName;
        this.id = id;
    }

    public Class getPropertyClass() {
        return introspector.getPropertyType(propertyName);
    }

    public Object getId() {
        return id;
    }

    public void setReference(Object entity, Object reference) {
        try {
            introspector.set(entity, propertyName, reference);
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
        if (introspector != null ? !introspector.equals(that.introspector) : that.introspector != null) return false;
        if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = introspector != null ? introspector.hashCode() : 0;
        result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
