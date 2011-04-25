package org.ormunit.entity;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 25.04.11
 * Time: 21:04
 */
public class NoGetterPrivateTestEntity extends SourceEntity{

    private String value;

    private String value2;

    public void setFieldNoGetter(String value){
        this.value = value;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }
}
