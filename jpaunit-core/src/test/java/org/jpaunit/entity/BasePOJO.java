package org.jpaunit.entity;

import javax.persistence.Id;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 22:05
 */
public class BasePOJO {

    @Id
    private int integerValue;


    public int getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(int intValue) {
        this.integerValue = intValue;
    }

}
