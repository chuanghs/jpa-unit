package org.ormunit.entity;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 22:26
 */
public class SimplePOJO2 {

    private String stringValue;


    private int intValue;

    public SimplePOJO2() {

    }

    public SimplePOJO2(String s, int i) {
        this.stringValue = s;
        this.intValue = i;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplePOJO2 that = (SimplePOJO2) o;

        if (intValue != that.intValue) return false;
        if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = stringValue != null ? stringValue.hashCode() : 0;
        result = 31 * result + intValue;
        return result;
    }

    @Override
    public String toString() {
        return "SimplePOJO2{" +
                "stringValue='" + stringValue + '\'' +
                ", intValue=" + intValue +
                '}';
    }
}
