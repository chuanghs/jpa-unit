package org.ormunit.live;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.entity.EntityWith2Ids;
import org.ormunit.entity.FieldAccessEntity;
import org.ormunit.entity.PropertyAccessEntity;
import org.ormunit.junit.JPAHelper;
import org.ormunit.junit.JPAUnitTestCase;

import javax.xml.bind.JAXBException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 21:37
 */
@RunWith(MockitoJUnitRunner.class)
public class JPAUnitComposedIdsTest extends JPAUnitTestCase {

    public JPAUnitComposedIdsTest() {
        super("ormunit-jpa-composed-ids");
    }

    @Test
    public void testTransaction() throws JAXBException {
        assertTrue(getEm().getTransaction().isActive());

        assertEquals(5, JPAHelper.getManagedTypes(getClass(), "ormunit-jpa").size());
    }

    @Test
    public void testReadAllEntities() {
        EntityWith2Ids ids = new EntityWith2Ids();
        ids.setId1(1);
        ids.setId2(2);


        assertNotNull(getEm().find(EntityWith2Ids.class, ids));
    }


    @Test
    public void testFindById() {

        FieldAccessEntity pae = getEm().getReference(FieldAccessEntity.class, 4);


        assertNotNull(pae);

    }


}
