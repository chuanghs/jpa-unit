package org.ormunit;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.node.entity.accessor.EntityAccessor;
import org.ormunit.entity.FieldAccessEntity;
import org.ormunit.entity.PrimaryKey;
import org.ormunit.entity.PropertyAccessEntity;
import org.ormunit.exception.EntityDefinitionException;

import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.IdClass;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 18:51
 */
@RunWith(MockitoJUnitRunner.class)
public class JPAORMProviderTest {


    @Mock
    private EntityManager em;


    @Test
    public void testGetAccessor() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);

        EntityAccessor fieldAccessor = provider.getAccessor(FieldAccessEntity.class, null);


        FieldAccessEntity fieldAccessEntity = new FieldAccessEntity();
        fieldAccessEntity.setIntegerValue(2);


        fieldAccessor.set(fieldAccessEntity, "integerValue", 1);

        assertEquals(1, fieldAccessEntity.getIntegerValue());


    }


    @Test
    public void testGetIdType() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);

        try {
            assertEquals(Integer.class, provider.getIdType(PropertyAccessEntity.class));
        } catch (EntityDefinitionException entityDefinitionException) {
            entityDefinitionException.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            assertEquals(int.class, provider.getIdType(FieldAccessEntity.class));
        } catch (EntityDefinitionException entityDefinitionException) {
            entityDefinitionException.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    private class SubPropertyAccessEntity extends PropertyAccessEntity {
    }

    @Test
    public void testGetIdTypeOfSubClass() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);
        EntityAccessor fieldAccessor = provider.getAccessor(SubPropertyAccessEntity.class, null);
        try {
            assertEquals(Integer.class, provider.getIdType(SubPropertyAccessEntity.class));
        } catch (EntityDefinitionException entityDefinitionException) {
            entityDefinitionException.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        SubPropertyAccessEntity entity = new SubPropertyAccessEntity();
        entity.setId(2);
        fieldAccessor.set(entity, "id", 1);

        assertEquals((Integer) 1, entity.getId());

    }

    private class EmbeddedIdEntity {
        @EmbeddedId
        private PrimaryKey pk;
    }

    @IdClass(PrimaryKey.class)
    private class IdClassEntity {

        @Id
        private String stringValue;

    }

    @Test
    public void testGetIdTypeEmbeddedId() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);
        try {
            assertEquals(PrimaryKey.class, provider.getIdType(EmbeddedIdEntity.class));
        } catch (EntityDefinitionException entityDefinitionException) {
            entityDefinitionException.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testGetIdTypeIdClass() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);
        try {
            assertEquals(PrimaryKey.class, provider.getIdType(IdClassEntity.class));
        } catch (EntityDefinitionException entityDefinitionException) {
            entityDefinitionException.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testGetIdIdClass() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);

        IdClassEntity entity = new IdClassEntity();
        entity.stringValue = "value";
        PrimaryKey id = (PrimaryKey) provider.getId(entity);

        assertEquals("value", id.getStringValue());
    }

    @Test
    public void testSetIdIdClass() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);

        IdClassEntity entity = new IdClassEntity();

        PrimaryKey id = new PrimaryKey();
        id.setStringValue("value");

        provider.setId(entity, id);

        assertEquals("value", entity.stringValue);
    }

    @Test
    public void testSetIdField() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);
        FieldAccessEntity entity = new FieldAccessEntity();
        provider.setId(entity, 1);
        assertEquals(1, entity.getIntegerValue());
    }

    @Test
    public void testGetIdField() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);
        FieldAccessEntity entity = new FieldAccessEntity();
        assertEquals(0, provider.getId(entity));
        entity.setIntegerValue(-1);
        assertEquals(entity.getIntegerValue(), provider.getId(entity));
    }

    @Test
    public void testGetIdProperty() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);
        PropertyAccessEntity entity = new PropertyAccessEntity();
        Assert.assertNull(provider.getId(entity));
        entity.setId(-1);
        assertEquals(entity.getId(), provider.getId(entity));
    }
}
