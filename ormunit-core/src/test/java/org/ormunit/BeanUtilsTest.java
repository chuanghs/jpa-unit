package org.ormunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.entity.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 24.04.11
 * Time: 20:39
 */
@RunWith(JUnit4.class)
public class BeanUtilsTest {


    @Test
    public void testGetField() throws NoSuchFieldException {
        BeanUtils beanUtils = new BeanUtils();

        Field f = beanUtils.getField(SimplePOJO.class, "doubleValue");

        assertNotNull(f);
        assertEquals("doubleValue", f.getName());

        f = beanUtils.getField(SimplePOJO.class, "boolTestValue");
        assertNotNull(f);
        assertEquals("boolTestValue", f.getName());

    }

    @Test
    public void testGetProperty() {
        BeanUtils beanUtils = new BeanUtils();

        PropertyDescriptor pd = beanUtils.getProperty(SimplePOJO.class, "doubleValue");

        assertNotNull(pd);
        assertEquals("doubleValue", pd.getName());

        pd = beanUtils.getProperty(SimplePOJO.class, "boolTestValue");
        assertNotNull(pd);
        assertEquals("boolTestValue", pd.getName());
    }


    @Test
    public void testGetFieldAnnotatedBy() {
        Set<Field> fields = new BeanUtils().getFieldsAnnotatedWith(SourceEntity.class, FooAnnotation.class);

        assertEquals(1, fields.size());
        assertEquals("field1", fields.iterator().next().getName());

    }

    @Test
    public void testGetPropertieAnnotatedBy() {
        Set<PropertyDescriptor> fields = new BeanUtils().getPropertiesAnnotatedWith(SourceEntity.class, FooAnnotation.class);

        assertEquals(1, fields.size());
        assertEquals("field2", fields.iterator().next().getName());
    }


    @Test
    public void testCopyFields() {
        SourceEntity sourceEntity = spy(new SourceEntity());
        sourceEntity.field1 = 1;
        sourceEntity.field2 = "2";
        TargetEntity targetEntity = spy(new TargetEntity());


        new BeanUtils().copyFieldValues(sourceEntity, targetEntity, TargetEntity.class);


        assertEquals(1, targetEntity.field1);
        assertEquals("2", targetEntity.field2);

    }

    @Test
    public void testCopyProperties() {
        SourceEntity sourceEntity = spy(new SourceEntity());
        sourceEntity.field1 = 1;
        sourceEntity.field2 = "2";
        TargetEntity targetEntity = spy(new TargetEntity());


        new BeanUtils().copyPropertyValues(sourceEntity, targetEntity, TargetEntity.class);


        verify(sourceEntity).getField1();
        verify(sourceEntity).getField2();

        verify(targetEntity).setField1(eq(1));
        verify(targetEntity).setField2(eq("2"));
    }

    @Test
    public void testGetPropertiesWithoutGetter(){
        NoGetterPrivateTestEntity source = new NoGetterPrivateTestEntity();
        source.setValue2("value");

        NoGetterPrivateTestEntity target = new NoGetterPrivateTestEntity();
        new BeanUtils().copyFieldValues(source, target, NoGetterPrivateTestEntity.class);

        assertEquals("value", target.getValue2());
    }
}
