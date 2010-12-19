package org.jpaunit.command;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 20:14
 */
public class EntityReference {

    private final PropertyDescriptor propertyDescriptor;

    private final Object id;
    private final Object entity;

    public EntityReference(Object entity, String propertyName, Object id) throws IntrospectionException {
        this.entity = entity;
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors();
        PropertyDescriptor propertyDescriptor = null;
        for (PropertyDescriptor pd : propertyDescriptors){
            if (pd.getName().equals(propertyName)){
                propertyDescriptor = pd;
                break;
            }
        }
        if (propertyDescriptor == null)
            throw new RuntimeException("non existing property: "+propertyName+" of class: "+entity.getClass().getCanonicalName());

        this.propertyDescriptor = propertyDescriptor;
        this.id = id;
    }

    public EntityReference(Object entity, PropertyDescriptor propertyDescriptor, Object id) {
        this.entity = entity;
        this.propertyDescriptor = propertyDescriptor;
        this.id = id;
    }

    public Class getPropertyClass() {
        return propertyDescriptor.getPropertyType();
    }

    public Object getId() {
        return id;
    }

    public void setReference(Object entity, Object reference) {
        //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public String toString() {
        return "EntityReference{" +
                ", entity=" + entity.hashCode() +
                ", property=" + propertyDescriptor.getName() +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityReference that = (EntityReference) o;

        if (entity != null ? !entity.equals(that.entity) : that.entity != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (propertyDescriptor != null ? !propertyDescriptor.getName().equals(that.propertyDescriptor!=null?that.propertyDescriptor.getName():null) : that.propertyDescriptor != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = propertyDescriptor != null ? propertyDescriptor.getName().hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (entity != null ? entity.hashCode() : 0);
        return result;
    }
}
