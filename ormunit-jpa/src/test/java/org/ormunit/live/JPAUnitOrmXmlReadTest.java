package org.ormunit.live;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.jpa.unit.XmlPersistenceUnit;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 16.06.11
 * Time: 16:25
 */
@RunWith(JUnit4.class)
public class JPAUnitOrmXmlReadTest {
    public JPAUnitOrmXmlReadTest() {

    }

    @Test
    public void testDefaultOrm() {
        Set<Class<?>> managedTypes = new XmlPersistenceUnit(getClass(), "ormunit-jpa-default-orm").getManagedTypes();
        Assert.assertEquals(1, managedTypes.size());
    }

    @Test
    public void testProvidedOrm() {
        Set<Class<?>> managedTypes = new XmlPersistenceUnit(getClass(), "ormunit-jpa-provided-orm").getManagedTypes();
        Assert.assertEquals(1, managedTypes.size());
    }

    @Test
    public void testProvidedOrmSlashesBackslashes() {
        Set<Class<?>> managedTypes = new XmlPersistenceUnit(getClass(), "ormunit-jpa-provided-orm-back-slashes").getManagedTypes();
        Assert.assertEquals(1, managedTypes.size());
    }
}
