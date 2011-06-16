package org.ormunit.live;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.junit.JPAHelper;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 16.06.11
 * Time: 16:25
 *
 */
@RunWith(JUnit4.class)
public class JPAUnitOrmXml {
    public JPAUnitOrmXml() {

    }

    @Test
    public void testDefaultOrm() {
        Set<Class<?>> managedTypes = JPAHelper.getManagedTypes(getClass(), "ormunit-jpa-default-orm");
        Assert.assertEquals(1, managedTypes.size());
    }

    @Test
    public void testProvidedOrm() {
        Set<Class<?>> managedTypes = JPAHelper.getManagedTypes(getClass(), "ormunit-jpa-provided-orm");
        Assert.assertEquals(1, managedTypes.size());
    }

    @Test
    public void testProvidedOrmSlashesBackslashes() {
        Set<Class<?>> managedTypes = JPAHelper.getManagedTypes(getClass(), "ormunit-jpa-provided-orm-back-slashes");
        Assert.assertEquals(1, managedTypes.size());
    }
}
