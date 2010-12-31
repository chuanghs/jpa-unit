package org.ormunit.entity;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.exception.ORMEntityAccessException;
import org.ormunit.exception.ORMUnitInstantiationException;

import java.util.Collection;

import static org.mockito.Mockito.*;

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

    @Test
    public void testGetProperty() {
        PropertyAccessor pa = new PropertyAccessor(SimplePOJO.class);

        SimplePOJO pojo = spy(new SimplePOJO());
        pojo.setStringValue("someStringValue");

        pa.get(pojo, "stringValue");

        verify(pojo, times(1)).getStringValue();
    }

    @Test
    public void testField() {
        FieldAccessor pa = new FieldAccessor(SimplePOJO.class);

        SimplePOJO pojo = new SimplePOJO();
        pojo.setStringValue("someStringValue");
        Assert.assertEquals(pojo.getStringValue(), pa.get(pojo, "stringValue"));
    }

    @Test
    public void testGetCollectionParameterType_Property() {
        PropertyAccessor pa = new PropertyAccessor(SimplePOJO.class);
        Assert.assertEquals(SimplePOJO2.class, pa.getCollectionParameterType("collection"));
    }

    @Test
    public void testGetCollectionParameterType2_Property() {
        PropertyAccessor pa = new PropertyAccessor(SimplePOJO.class);
        Assert.assertEquals(SimplePOJO2.class, pa.getCollectionParameterType("abstractCollection"));
    }

    @Test
    public void testGetCollectionParameterType_Field() {
        FieldAccessor pa = new FieldAccessor(SimplePOJO.class);
        Assert.assertEquals(SimplePOJO2.class, pa.getCollectionParameterType("collection"));
    }

    @Test
    public void testInstantiations() {

        PropertyAccessor pa = new PropertyAccessor(SimplePOJO.class);
        try {
            pa.newInstance("abstractCollection");
            Assert.fail();
        } catch (ORMUnitInstantiationException e) {
        }

        try {
            pa.newInstance("nonexistingproperty");
            Assert.fail();
        } catch (ORMUnitInstantiationException e) {
        }

    }

    public class GenericTestClass<T> {

        private Collection<? extends String> extendsString;

        private Collection<? super String> superString;

        private Collection<? extends T> extendsT;

        private Collection<? super T> superT;

        private Collection<T> t;

    }


    @Test
    public void testExtractingClass1() {

        FieldAccessor fa = new FieldAccessor(GenericTestClass.class);

        Assert.assertEquals(String.class, fa.getCollectionParameterType("extendsString"));
        Assert.assertEquals(String.class, fa.getCollectionParameterType("superString"));

        Assert.assertEquals(Object.class, fa.getCollectionParameterType("extendsT"));
        Assert.assertEquals(Object.class, fa.getCollectionParameterType("superT"));
        Assert.assertEquals(Object.class, fa.getCollectionParameterType("t"));


    }

    public class GenericTestSubClass extends GenericTestClass<SimplePOJO> {

        public <K extends SimplePOJO2> Collection<K> getCollectionK(){return null;}

    }

    @Test
    public void testExtractingClass2() {
        PropertyAccessor pa = new PropertyAccessor(GenericTestSubClass.class);

        Assert.assertEquals(SimplePOJO2.class, pa.getCollectionParameterType("collectionK"));
    }


    @Test
    public void testExtractingClass3() {

        FieldAccessor fa = new FieldAccessor(GenericTestSubClass.class);

        Assert.assertEquals(String.class, fa.getCollectionParameterType("extendsString"));
        Assert.assertEquals(String.class, fa.getCollectionParameterType("superString"));

        Assert.assertEquals(SimplePOJO.class, fa.getCollectionParameterType("extendsT"));
        Assert.assertEquals(SimplePOJO.class, fa.getCollectionParameterType("superT"));
        Assert.assertEquals(SimplePOJO.class, fa.getCollectionParameterType("t"));


    }

}
