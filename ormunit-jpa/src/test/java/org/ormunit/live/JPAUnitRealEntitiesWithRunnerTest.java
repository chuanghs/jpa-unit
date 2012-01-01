package org.ormunit.live;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ormunit.entity.FieldAccessEntity;
import org.ormunit.entity.PropertyAccessEntity;
import org.ormunit.jpa.annotations.Em;
import org.ormunit.jpa.annotations.JPAUnitTestCase;
import org.ormunit.jpa.junit.JPAUnitRunner;
import org.ormunit.jpa.persistenceunit.XmlPersistenceUnit;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 21:37
 */
@RunWith(JPAUnitRunner.class)
@JPAUnitTestCase(unitName = "ormunit-jpa", ormUnitFileName = "JPAUnitRealEntitiesTest.xml")
public class JPAUnitRealEntitiesWithRunnerTest {

    @Em
    private EntityManager em;

    public EntityManager getEm() {
        return em;
    }

    public JPAUnitRealEntitiesWithRunnerTest() {
        super();
    }

    @Test
    public void testTransaction() throws JAXBException {
        assertTrue(getEm().getTransaction().isActive());

        assertEquals(5, new XmlPersistenceUnit(getClass(), "ormunit-jpa").getManagedTypes().size());
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
