package org.ormunit;

import junit.framework.Assert;
import org.junit.Test;
import org.ormunit.entity.SimplePOJO;
import org.ormunit.entity.SimplePOJO2;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 11:51
 */
public class ORMUnitIntrospectorTest {
    @Test
    public void testEquals() throws Exception {
        Assert.assertEquals(new ORMUnitIntrospector(SimplePOJO.class), new ORMUnitIntrospector(SimplePOJO.class));
        Assert.assertFalse(new ORMUnitIntrospector(SimplePOJO.class).equals(new ORMUnitIntrospector(SimplePOJO2.class)));
    }

    @Test
    public void testHashCode() throws Exception {
        Assert.assertTrue(new ORMUnitIntrospector(SimplePOJO.class).hashCode() == new ORMUnitIntrospector(SimplePOJO.class).hashCode());
        Assert.assertFalse(new ORMUnitIntrospector(SimplePOJO.class).hashCode() == new ORMUnitIntrospector(SimplePOJO2.class).hashCode());
    }
}
