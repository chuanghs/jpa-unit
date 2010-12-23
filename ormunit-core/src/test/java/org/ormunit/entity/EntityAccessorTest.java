package org.ormunit.entity;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.exception.ORMEntityAccessException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 23:32
 */
@RunWith(JUnit4.class)
public class EntityAccessorTest {


    class FieldEntity {

        private int intField;
        private String stringField;

    }

    class PropertyEntity {
        private boolean booleanValue;

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
    }


    FieldAccessor fieldAccessor = new FieldAccessor(FieldEntity.class);

    @Test
    public void testEquals_hashcode() {
        FieldAccessor fieldAccessor1 = new FieldAccessor(FieldEntity.class);
        FieldAccessor fieldAccessor2 = new FieldAccessor(PropertyEntity.class);

        Assert.assertEquals(fieldAccessor, fieldAccessor1);
        Assert.assertEquals(fieldAccessor1.hashCode(), fieldAccessor.hashCode());

        Assert.assertTrue(!fieldAccessor.equals(fieldAccessor2));


    }

    @Test
    public void testFieldAccessor() {


        Assert.assertEquals((Class) int.class, fieldAccessor.getType("intField"));
    }

    @Test(expected = ORMEntityAccessException.class)
    public void testNewInstance() {

        fieldAccessor.newInstance("intField");

    }

    @Test
    public void testSet() {

        FieldEntity simplePOJO = new FieldEntity();
        simplePOJO.intField = 2;
        fieldAccessor.set(simplePOJO, "intField", 1);
        Assert.assertEquals(1, simplePOJO.intField);
    }

    @Test(expected = ORMEntityAccessException.class)
    public void testSetNonExistingField() {

        FieldEntity simplePOJO = new FieldEntity();
        fieldAccessor.set(simplePOJO, "nonExistingField", 1);
    }

}
