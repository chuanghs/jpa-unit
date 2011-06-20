package org.ormunit;

import com.sun.java.xml.ns.persistence.orm.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.entity.*;
import org.ormunit.jpa.entityinspector.EntityInspector;
import org.ormunit.jpa.entityinspector.EntityMappingsEntityInspector;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak (tomasz.krzyzak@gmail.com)
 * Date: 18.06.11
 * Time: 16:34
 */
// TODO: test getschema
@RunWith(MockitoJUnitRunner.class)
public class EntityMappingsEntityInspectorTest {

    @Mock
    private EntityInspector backupEntityInspector;

    private EntityMappingsEntityInspector ormFileEntityInspector;

    private EntityMappings entityMappings;

    @Before
    public void before() {
        entityMappings = new EntityMappings();
        ormFileEntityInspector = spy(new EntityMappingsEntityInspector(entityMappings, backupEntityInspector));
    }

    @Test
    public void testGetSchema() {
        Entity entity = new Entity();
        entity.setClazz(Employee.class.getCanonicalName());
        Table table = new Table();
        table.setSchema("schema");
        entity.setTable(table);
        entityMappings.getEntity().add(entity);

        assertEquals(table.getSchema(), ormFileEntityInspector.getSchemaName(Employee.class));
    }


    @Test
    public void testGetSchemaDelegated() {
        ormFileEntityInspector.getSchemaName(Employee.class);
        verify(backupEntityInspector).getSchemaName(eq(Employee.class));
    }


    @Test
    public void tesGettAccessType() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Employee.class.getCanonicalName());
        entityMappings.getEntity().add(entity);

        assertEquals(AccessType.FIELD, ormFileEntityInspector.getAccessTypeOfClass(Employee.class));
    }

    @Test
    public void tesGettAccessTypeDelegated() {
        Entity entity = new Entity();
        entity.setClazz(Employee.class.getCanonicalName());
        entityMappings.getEntity().add(entity);


        ormFileEntityInspector.getAccessTypeOfClass(Employee.class);
        verify(backupEntityInspector, times(1)).getAccessTypeOfClass(eq(Employee.class));
    }

    @Test
    public void testGetIdTypeOfEntityClass() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Employee.class.getCanonicalName());
        IdClass idclass = new IdClass();
        idclass.setClazz(EmployeeId.class.getCanonicalName());
        entity.setIdClass(idclass);
        entityMappings.getEntity().add(entity);

        assertEquals(EmployeeId.class, ormFileEntityInspector.getIdType(Employee.class));
        verify(backupEntityInspector, times(0)).getIdType(eq(Employee.class));
    }


    @Test
    public void testGetIdTypeOfEntityClass_Property() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.PROPERTY);
        entity.setClazz(PropertyAccessEntity.class.getCanonicalName());
        Attributes value = new Attributes();
        Id id = new Id();
        id.setName("id");
        value.getId().add(id);
        entity.setAttributes(value);

        entityMappings.getEntity().add(entity);

        assertEquals(Integer.class, ormFileEntityInspector.getIdType(PropertyAccessEntity.class));
        verify(backupEntityInspector, times(0)).getIdType(eq(PropertyAccessEntity.class));
    }


    @Test
    public void testGetIdTypeOfEntityClass_Field() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD );
        entity.setClazz(FieldAccessEntity.class.getCanonicalName());
        Attributes value = new Attributes();
        Id id = new Id();
        id.setName("integerValue");
        value.getId().add(id);
        entity.setAttributes(value);

        entityMappings.getEntity().add(entity);

        assertEquals(int.class, ormFileEntityInspector.getIdType(FieldAccessEntity.class));
        verify(backupEntityInspector, times(0)).getIdType(eq(FieldAccessEntity.class));
    }

    @Test
    public void testGetIdTypeOfEntityClassDelegated() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Employee.class.getCanonicalName());
        entityMappings.getEntity().add(entity);

        ormFileEntityInspector.getIdType(Employee.class);
        verify(backupEntityInspector, times(1)).getIdType(eq(Employee.class));
    }

    @Test
    public void testGetIdProperty() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.PROPERTY);
        entity.setClazz(PropertyAccessEntity.class.getCanonicalName());
        Id id1 = new Id();
        id1.setName("id");
        Attributes attributes = new Attributes();
        attributes.getId().add(id1);
        entity.setAttributes(attributes);
        entityMappings.getEntity().add(entity);

        PropertyDescriptor idProperty = ormFileEntityInspector.getIdProperty(PropertyAccessEntity.class);
        assertNotNull(idProperty);
        assertEquals("id", idProperty.getName());
        verify(backupEntityInspector, times(0)).getIdProperty(eq(PropertyAccessEntity.class));
    }

    @Test
    public void testGetIdPropertyDelegated() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.PROPERTY);
        entity.setClazz(PropertyAccessEntity.class.getCanonicalName());
        entityMappings.getEntity().add(entity);

        PropertyDescriptor idProperty = ormFileEntityInspector.getIdProperty(PropertyAccessEntity.class);
        assertNull(idProperty);
        verify(backupEntityInspector, times(1)).getIdProperty(eq(PropertyAccessEntity.class));
    }

    @Test
    public void testGetIdField() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Auction.class.getCanonicalName());
        Id id1 = new Id();
        id1.setName("id");
        Attributes attributes = new Attributes();
        attributes.getId().add(id1);
        entity.setAttributes(attributes);
        entityMappings.getEntity().add(entity);

        Field idProperty = ormFileEntityInspector.getIdField(Auction.class);
        assertNotNull(idProperty);
        assertEquals("id", idProperty.getName());
        verify(backupEntityInspector, times(0)).getIdField(eq(Auction.class));
    }

    @Test
    public void testGetIdFieldDelegated() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Auction.class.getCanonicalName());
        entityMappings.getEntity().add(entity);

        Field idProperty = ormFileEntityInspector.getIdField(Auction.class);
        assertNull(idProperty);
        verify(backupEntityInspector, times(1)).getIdField(eq(Auction.class));
    }

    @Test
    public void testIsIdGenerated() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Auction.class.getCanonicalName());
        Id id1 = new Id();
        id1.setName("id");
        GeneratedValue generatedValue = new GeneratedValue();
        id1.setGeneratedValue(generatedValue);
        Attributes attributes = new Attributes();
        attributes.getId().add(id1);
        entity.setAttributes(attributes);
        entityMappings.getEntity().add(entity);

        boolean generated = ormFileEntityInspector.isIdGenerated(Auction.class);

        assertTrue(generated);
        verify(backupEntityInspector, times(0)).isIdGenerated(eq(Auction.class));
    }

    @Test
    public void testIsIdGeneratedDelegated() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Auction.class.getCanonicalName());
        entityMappings.getEntity().add(entity);

        ormFileEntityInspector.isIdGenerated(Auction.class);
        verify(backupEntityInspector, times(1)).isIdGenerated(eq(Auction.class));
    }


    @Test
    public void testGetIdType() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Auction.class.getCanonicalName());
        Id id1 = new Id();
        id1.setName("id");
        Attributes attributes = new Attributes();
        attributes.getId().add(id1);
        entity.setAttributes(attributes);
        entityMappings.getEntity().add(entity);

        Class idClass = ormFileEntityInspector.getIdType(Auction.class);

        assertNotNull(idClass);
        assertEquals(Integer.class, idClass);
        verify(backupEntityInspector, times(0)).getIdType(eq(Auction.class));
    }

    @Test
    public void testGetIdTypeDelegated() {
        Entity entity = new Entity();
        entity.setAccess(AccessType.FIELD);
        entity.setClazz(Auction.class.getCanonicalName());
        entityMappings.getEntity().add(entity);

        ormFileEntityInspector.getIdType(Auction.class);
        verify(backupEntityInspector, times(1)).getIdType(eq(Auction.class));
    }

}
