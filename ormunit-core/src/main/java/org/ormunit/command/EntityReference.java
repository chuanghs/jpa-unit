package org.ormunit.command;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 20:14
 */
public class EntityReference {


    public static enum ReferenceType {
        DB,
        ORMUNIT
    }

    private final String propertyName;
    private final Object id;
    private final ReferenceType referenceType;

    public EntityReference(String propertyName, Object id, ReferenceType referenceType) {
        this.propertyName = propertyName;
        this.id = id;
        this.referenceType = referenceType;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
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
                ", referenceType=" + referenceType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityReference that = (EntityReference) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (referenceType != null ? !referenceType.equals(that.referenceType) : that.referenceType != null) return false;
        if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (propertyName != null ? propertyName.hashCode() : 0);
        result = 31 * result + (referenceType != null ? referenceType.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
