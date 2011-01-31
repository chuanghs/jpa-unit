package org.ormunit;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.entity.EntityAccessor;
import org.ormunit.entity.FieldAccessEntity;
import org.ormunit.entity.PrimaryKey;
import org.ormunit.entity.PropertyAccessEntity;

import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;

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

        EntityAccessor fieldAccessor = provider.getAccessor(FieldAccessEntity.class);


        FieldAccessEntity fieldAccessEntity = new FieldAccessEntity();
        fieldAccessEntity.setIntegerValue(2);


        fieldAccessor.set(fieldAccessEntity, "integerValue", 1);

        Assert.assertEquals(1, fieldAccessEntity.getIntegerValue());


    }


    @Test
    public void testGetIdType() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);

        Assert.assertEquals(int.class, provider.getIdType(FieldAccessEntity.class));
        Assert.assertEquals(Integer.class, provider.getIdType(PropertyAccessEntity.class));
    }


    private class SubPropertyAccessEntity extends PropertyAccessEntity {
    }

    @Test
    public void testGetIdTypeOfSubClass() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);
        EntityAccessor fieldAccessor = provider.getAccessor(SubPropertyAccessEntity.class);
        Assert.assertEquals(Integer.class, provider.getIdType(SubPropertyAccessEntity.class));

        SubPropertyAccessEntity entity = new SubPropertyAccessEntity();
        entity.setId(2);
        fieldAccessor.set(entity, "id", 1);

        Assert.assertEquals((Integer) 1, entity.getId());

    }

    private class EmbeddedIdEntity {
        @EmbeddedId
        private PrimaryKey pk;
    }

    @Test
    public void testGetIdTypeEmbeddedId() throws Exception {
        JPAORMProvider provider = new JPAORMProvider(em);
        Assert.assertEquals(PrimaryKey.class, provider.getIdType(EmbeddedIdEntity.class));
    }
}
