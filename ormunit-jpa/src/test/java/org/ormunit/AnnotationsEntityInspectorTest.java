package org.ormunit;

import com.sun.java.xml.ns.persistence.orm.AccessType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.entity.*;
import org.ormunit.jpa.entityinspector.AnnotationsEntityInspector;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 20.06.11
 * Time: 20:15
 */
@RunWith(MockitoJUnitRunner.class)
public class AnnotationsEntityInspectorTest {

    private AnnotationsEntityInspector entityInspector;

    @Before
    public void before() {
        entityInspector = new AnnotationsEntityInspector();
    }

    @Test
    public void testGetIdClassValue() throws Exception {
        assertNull(entityInspector.getIdClassValue(Auction.class));
        assertEquals(EmployeeId.class, entityInspector.getIdClassValue(Employee.class));
    }

    @Test
    public void testGetIdType() throws Exception {
        assertEquals(Integer.class, entityInspector.getIdType(Auction.class));
        assertEquals(EmployeeId.class, entityInspector.getIdType(Employee.class));
    }

    @Test
    public void testIdProperty() {
        assertNull(entityInspector.getIdProperty(FieldAccessEntity.class));
        PropertyDescriptor idProperty = entityInspector.getIdProperty(PropertyAccessEntity.class);
        assertNotNull(idProperty);
        assertEquals("id", idProperty.getName());
    }

    @Test
    public void testIdField() {
        assertNull(entityInspector.getIdField(PropertyAccessEntity.class));
        Field idField = entityInspector.getIdField(FieldAccessEntity.class);
        assertNotNull(idField);
        assertEquals("integerValue", idField.getName());
    }

    @Test
    public void testGetAccessType(){
        assertEquals(AccessType.PROPERTY, entityInspector.getAccessTypeOfClass(PropertyAccessEntity.class));
        assertEquals(AccessType.FIELD, entityInspector.getAccessTypeOfClass(FieldAccessEntity.class));
    }

    @Test
    public void testGetSchema(){
        assertNull(entityInspector.getSchemaName(Auction.class));
        assertNotNull(entityInspector.getSchemaName(PropertyAccessEntity.class));
    }

}
