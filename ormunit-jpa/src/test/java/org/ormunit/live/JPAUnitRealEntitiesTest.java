package org.ormunit.live;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.jpa.unit.XmlPersistenceUnit;
import org.ormunit.entity.FieldAccessEntity;
import org.ormunit.entity.PropertyAccessEntity;
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
public class JPAUnitRealEntitiesTest extends JPAUnitTestCase {

    public JPAUnitRealEntitiesTest() {
        super("ormunit-jpa");
    }

    @Test
    public void testTransaction() throws JAXBException {
        assertTrue(getEm().getTransaction().isActive());

        assertEquals(4, new XmlPersistenceUnit(getClass(), "ormunit-jpa").getManagedTypes().size());
    }

    @Test
    public void testReadAllEntities() {
        List resultList = getEm().createQuery("select o from " + PropertyAccessEntity.class.getSimpleName() + " o").getResultList();
        assertEquals(4, resultList.size());

        resultList = getEm().createQuery("select o from " + FieldAccessEntity.class.getSimpleName() + " o").getResultList();
        assertEquals(5, resultList.size());
    }

    @Test
    public void testReadAllEntities2() {
        List resultList = getEm().createQuery("select o from " + PropertyAccessEntity.class.getSimpleName() + " o").getResultList();
        assertEquals(4, resultList.size());

        resultList = getEm().createQuery("select o from " + FieldAccessEntity.class.getSimpleName() + " o").getResultList();
        assertEquals(5, resultList.size());
    }

    @Test
    public void testReplaceReference() {
        List resultList = getEm().createQuery("select o from " + FieldAccessEntity.class.getSimpleName() + " o").getResultList();
        FieldAccessEntity fae = getEm().getReference(FieldAccessEntity.class, 1);
        fae.setComplexType(null);

        getEm().persist(fae);

        getEm().flush();
        getEm().clear();

        fae = getEm().getReference(FieldAccessEntity.class, 1);

        assertNull(fae.getComplexType());

    }

    @Test
    public void testFindById() {

        FieldAccessEntity pae = getEm().getReference(FieldAccessEntity.class, 4);


        assertNotNull(pae);

    }


}
