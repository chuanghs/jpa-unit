package org.jpaunit.node;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 18:13
 */
public class SimplePOJO {


    private int integerValue;
    private double doubleValue;
    private boolean booleanValue;
    private String stringValue;
    private Timestamp timestampValue;
    private Date dateValue;

    private SimplePOJO2 complexType;

    public void setIntegerValue(int i) {
        integerValue = i;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplePOJO that = (SimplePOJO) o;

        if (booleanValue != that.booleanValue) return false;
        if (Double.compare(that.doubleValue, doubleValue) != 0) return false;
        if (integerValue != that.integerValue) return false;
        if (complexType != null ? !complexType.equals(that.complexType) : that.complexType != null) return false;
        if (dateValue != null ? !dateValue.equals(that.dateValue) : that.dateValue != null) return false;
        if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null) return false;
        if (timestampValue != null ? !timestampValue.equals(that.timestampValue) : that.timestampValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = integerValue;
        temp = doubleValue != +0.0d ? Double.doubleToLongBits(doubleValue) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (booleanValue ? 1 : 0);
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        result = 31 * result + (timestampValue != null ? timestampValue.hashCode() : 0);
        result = 31 * result + (dateValue != null ? dateValue.hashCode() : 0);
        result = 31 * result + (complexType != null ? complexType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimplePOJO{" +
                "integerValue=" + integerValue +
                ", doubleValue=" + doubleValue +
                ", booleanValue=" + booleanValue +
                ", stringValue='" + stringValue + '\'' +
                ", timestampValue=" + timestampValue +
                ", dateValue=" + dateValue +
                ", complexType=" + complexType +
                '}';
    }
}
