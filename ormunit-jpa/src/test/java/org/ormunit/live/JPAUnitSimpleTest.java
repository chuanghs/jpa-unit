package org.ormunit.live;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
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
public class JPAUnitSimpleTest extends JPAUnitTestCase {

    public JPAUnitSimpleTest() {
        super("ormunit-jpa");
    }

    @Test
    public void testTransaction() throws JAXBException {
        assertTrue(getEm().getTransaction().isActive());

        assertEquals(2, JPAHelper.getManagedTypes(getClass(), "ormunit-jpa").size());
    }

    @Test
    public void testReadAllEntities() {
        List resultList = getEm().createQuery("select o from " + PropertyAccessEntity.class.getSimpleName() + " o").getResultList();
        assertEquals(4, resultList.size());

        resultList = getEm().createQuery("select o from " + FieldAccessEntity.class.getSimpleName() + " o").getResultList();
        assertEquals(5, resultList.size());
    }

    @Test
    public void testReplaceReference() {
        FieldAccessEntity fae = getEm().getReference(FieldAccessEntity.class, 1);
        fae.setComplexType(null);

        getEm().persist(fae);

        getEm().flush();
        getEm().clear();

        fae = getEm().getReference(FieldAccessEntity.class, 1);

        assertNull(fae.getComplexType());

    }

    @Test
    public void testReplaceReference2() {

        PropertyAccessEntity pae = getEm().getReference(PropertyAccessEntity.class, 4);
        FieldAccessEntity fae = getEm().getReference(FieldAccessEntity.class, 1);
        fae.setComplexType(pae);

        getEm().persist(fae);

        getEm().flush();
        getEm().clear();

        fae = getEm().getReference(FieldAccessEntity.class, 1);

        assertEquals(pae, fae.getComplexType());

    }

    @Test
    public void testPersist() {
        PropertyAccessEntity pae = getEm().getReference(PropertyAccessEntity.class, 4);
        FieldAccessEntity fae1 = new FieldAccessEntity();
        fae1.setIntegerValue(6);
        fae1.setComplexType(pae);
        getEm().persist(fae1);

        fae1 = new FieldAccessEntity();
        fae1.setIntegerValue(7);
        fae1.setComplexType(pae);
        getEm().persist(fae1);

        List resultList = getEm().createQuery("select o from " + FieldAccessEntity.class.getSimpleName() + " o").getResultList();

        assertEquals(7, resultList.size());

    }


}
