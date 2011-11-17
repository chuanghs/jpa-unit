package org.ormunit.entity;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 22:05
 */
@Entity
@Table(name = "FieldAccessEntity_schema.fieldaccessentity")
public class FieldAccessEntity {

    @Id
    private int integerValue;

    @ManyToOne
    @JoinColumn(name = "complextype_id")
    private PropertyAccessEntity complexType;


    public int getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(int intValue) {
        this.integerValue = intValue;
    }

    public PropertyAccessEntity getComplexType() {
        return complexType;
    }

    public void setComplexType(PropertyAccessEntity complexType) {
        this.complexType = complexType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldAccessEntity that = (FieldAccessEntity) o;

        if (integerValue != that.integerValue) return false;
        if (complexType != null ? !complexType.equals(that.complexType) : that.complexType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = integerValue;
        result = 31 * result + (complexType != null ? complexType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FieldAccessEntity{" +
                "integerValue=" + integerValue +
                ", complexType=" + complexType +
                '}';
    }
}
