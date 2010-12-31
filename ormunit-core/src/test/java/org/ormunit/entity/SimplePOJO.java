package org.ormunit.entity;

import java.sql.Timestamp;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 18:13
 */
public class SimplePOJO {

    private double doubleValue;
    private boolean booleanValue;
    private String stringValue;
    private Timestamp timestampValue;
    private Date dateValue;
    private Long longValue;
    private Float floatValue;

    private SimplePOJO2 complexType;
    private int integerValue;

    private Collection<SimplePOJO2> collection;

    private AbstractList<SimplePOJO2> abstractCollection;


    public int getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(int intValue) {
        this.integerValue = intValue;
    }


    public double getDoubleValue() {
        return doubleValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Timestamp getTimestampValue() {
        return timestampValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }


    public void setDoubleValue(double v) {
        doubleValue = v;
    }


    public void setBooleanValue(boolean b) {
        booleanValue = b;
    }


    public void setStringValue(String string) {
        stringValue = string;
    }

    public void setTimestampValue(Timestamp timestamp) {
        timestampValue = timestamp;
    }

    public void setDateValue(Date date) {
        dateValue = date;
    }


    public SimplePOJO2 getComplexType() {
        return complexType;
    }

    public void setComplexType(SimplePOJO2 complexType) {
        this.complexType = complexType;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }

    public Collection<SimplePOJO2> getCollection() {
        return collection;
    }

    public void setCollection(Collection<SimplePOJO2> collection) {
        this.collection = collection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplePOJO that = (SimplePOJO) o;

        if (booleanValue != that.booleanValue) return false;
        if (Double.compare(that.doubleValue, doubleValue) != 0) return false;
        if (getIntegerValue() != that.getIntegerValue()) return false;
        if (complexType != null ? !complexType.equals(that.complexType) : that.complexType != null) return false;
        if (dateValue != null ? !dateValue.equals(that.dateValue) : that.dateValue != null) return false;
        if (floatValue != null ? !floatValue.equals(that.floatValue) : that.floatValue != null) return false;
        if (longValue != null ? !longValue.equals(that.longValue) : that.longValue != null) return false;
        if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null) return false;
        if (timestampValue != null ? !timestampValue.equals(that.timestampValue) : that.timestampValue != null)
            return false;
        if (collection != null ? !sameElements(that.collection) : that.collection!=null) return false;

        return true;
    }

    private boolean sameElements(Collection<SimplePOJO2> collection) {
        if (collection == null )
            return false;
        if (this.collection.size()!=collection.size()){
            return false;
        }
        for (SimplePOJO2 pojo2 : this.collection){
            if (!collection.contains(pojo2))
                return false;
        }
        return true;
    }

    public AbstractList<SimplePOJO2> getAbstractCollection() {
        return abstractCollection;
    }

    public void setAbstractCollection(AbstractList<SimplePOJO2> abstractCollection) {
        this.abstractCollection = abstractCollection;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getIntegerValue();
        temp = doubleValue != +0.0d ? Double.doubleToLongBits(doubleValue) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (booleanValue ? 1 : 0);
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        result = 31 * result + (timestampValue != null ? timestampValue.hashCode() : 0);
        result = 31 * result + (dateValue != null ? dateValue.hashCode() : 0);
        result = 31 * result + (longValue != null ? longValue.hashCode() : 0);
        result = 31 * result + (floatValue != null ? floatValue.hashCode() : 0);
        result = 31 * result + (complexType != null ? complexType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimplePOJO{" +
                "integerValue=" + getIntegerValue() +
                ", doubleValue=" + doubleValue +
                ", booleanValue=" + booleanValue +
                ", stringValue='" + stringValue + '\'' +
                ", timestampValue=" + timestampValue +
                ", dateValue=" + dateValue +
                ", longValue=" + longValue +
                ", floatValue=" + floatValue +
                ", complexType=" + complexType +
                '}';
    }
}
