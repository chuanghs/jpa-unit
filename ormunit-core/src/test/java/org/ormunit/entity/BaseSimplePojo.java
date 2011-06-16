package org.ormunit.entity;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 27.03.11
 * Time: 14:43
 */
public abstract class BaseSimplePojo {

    public abstract double getDoubleValue();

    private boolean boolTestValue;


    public boolean getBoolTestValue(){
        return false;
    }
    public void setBoolTestValue(boolean testValue){

    }

}
