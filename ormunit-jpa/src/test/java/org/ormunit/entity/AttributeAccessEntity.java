package org.ormunit.entity;

import javax.persistence.Id;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 22:05
 */
public class AttributeAccessEntity {

    @Id
    private int integerValue;

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

        AttributeAccessEntity that = (AttributeAccessEntity) o;

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
        return "AttributeAccessEntity{" +
                "integerValue=" + integerValue +
                ", complexType=" + complexType +
                '}';
    }
}
