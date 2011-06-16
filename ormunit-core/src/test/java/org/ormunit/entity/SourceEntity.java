package org.ormunit.entity;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 24.04.11
 * Time: 21:08
 */
public class SourceEntity extends BaseSimplePojo {

    @FooAnnotation
    public int field1;

    public String field2;

    @Override
    public double getDoubleValue() {
        return 0;
    }

    public int getField1() {
        return field1;
    }

    public void setField1(int field1) {
        this.field1 = field1;
    }

    @FooAnnotation
    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }
}
