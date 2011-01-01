package org.ormunit.entity;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.exception.ORMEntityAccessException;
import org.ormunit.exception.ORMUnitInstantiationException;

import java.util.Collection;
import java.util.Map;

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

    @Test(expected = ORMUnitInstantiationException.class)
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

    public interface ExtraMap<K> extends Map<Integer, String> {

    }

    public class GenericTestClass<GTC1, GTC2> {

        private Collection<? extends String> extendsString;

        private Collection<? super String> superString;

        private Collection<? extends GTC1> extendsT;

        private Collection<? super GTC1> superT;

        private Collection<GTC1> t;

        private Map<GTC1, Integer> mapTInteger;

        private Map<GTC1, ? extends Integer> mapTextendsInteger;

        private Map<? extends GTC1, Integer> mapextendsTInteger;

        private ExtraMap<GTC2> extraMap;

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

    public class GenericTestSubClass<GTSC> extends GenericTestClass<SimplePOJO, GTSC> {

        private Collection<GTSC> z;

        public <K extends SimplePOJO2> Collection<K> getCollectionK() {
            return null;
        }

        public Collection<GTSC> getZ() {
            return z;
        }

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

        Assert.assertEquals(SimplePOJO.class, fa.getCollectionParameterType("superT"));
        Assert.assertEquals(SimplePOJO.class, fa.getCollectionParameterType("t"));

        Assert.assertEquals(SimplePOJO.class, fa.getCollectionParameterType("extendsT"));

    }

    public class GenericTestSubSubClass extends GenericTestSubClass<Integer> {

    }

    @Test
    public void testExtractingClass4() {
        PropertyAccessor pa = new PropertyAccessor(GenericTestSubSubClass.class);

        Assert.assertEquals(Integer.class, pa.getCollectionParameterType("z"));
    }

    @Test
    public void testExtractMapTypes(){
        FieldAccessor fa = new FieldAccessor(GenericTestClass.class);

        Class[] mapTIntegers = fa.getMapParameterTypes("mapTInteger");
        Assert.assertEquals(Object.class, mapTIntegers[0]);
        Assert.assertEquals(Integer.class, mapTIntegers[1]);

        mapTIntegers = fa.getMapParameterTypes("mapTextendsInteger");
        Assert.assertEquals(Object.class, mapTIntegers[0]);
        Assert.assertEquals(Integer.class, mapTIntegers[1]);

        mapTIntegers = fa.getMapParameterTypes("mapextendsTInteger");
        Assert.assertEquals(Object.class, mapTIntegers[0]);
        Assert.assertEquals(Integer.class, mapTIntegers[1]);

        mapTIntegers = fa.getMapParameterTypes("extraMap");
        Assert.assertEquals(Integer.class, mapTIntegers[0]);
        Assert.assertEquals(String.class, mapTIntegers[1]);
    }

}
