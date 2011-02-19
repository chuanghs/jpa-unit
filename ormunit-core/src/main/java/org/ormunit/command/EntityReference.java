package org.ormunit.command;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 20:14
 */
public class EntityReference {


    public static enum Type {
        DB,
        ORMUNIT
    }

    private final String propertyName;
    private final Object id;
    private final Type type;

    public EntityReference(String propertyName, Object id){
        this(propertyName, id, Type.DB);
    }

    public EntityReference(String propertyName, Object id, Type type) {
        this.propertyName = propertyName;
        this.id = id;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Object getId() {
        return id;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String toString() {
        return "EntityReference{" +
                ", property=" + propertyName +
                ", id=" + id +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityReference that = (EntityReference) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (propertyName != null ? propertyName.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
