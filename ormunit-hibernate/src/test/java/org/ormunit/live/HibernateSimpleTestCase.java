package org.ormunit.live;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.entity.FieldAccessEntity;
import org.ormunit.entity.PropertyAccessEntity;
import org.ormunit.junit.HibernateUnitTestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 22.02.11
 * Time: 21:18
 */
@RunWith(JUnit4.class)
public class HibernateSimpleTestCase extends HibernateUnitTestCase {
    public HibernateSimpleTestCase() {
        super("hibernate.cfg.xml", "HibernateSimpleTestCase.xml");
    }

    @Test
    public void testGetSession1() {
        assertEquals(4, getSession().createCriteria(PropertyAccessEntity.class).list().size());
        assertEquals(5, getSession().createCriteria(FieldAccessEntity.class).list().size());
    }

    @Test
    public void testGetSession2() {
        assertEquals(4, getSession().createCriteria(PropertyAccessEntity.class).list().size());
        assertEquals(5, getSession().createCriteria(FieldAccessEntity.class).list().size());
    }
}
