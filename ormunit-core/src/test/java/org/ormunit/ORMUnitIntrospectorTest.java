package org.ormunit;

import junit.framework.Assert;
import org.junit.Test;
import org.ormunit.entity.PropertyAccessor;
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
        Assert.assertEquals(new PropertyAccessor(SimplePOJO.class), new PropertyAccessor(SimplePOJO.class));
        Assert.assertFalse(new PropertyAccessor(SimplePOJO.class).equals(new PropertyAccessor(SimplePOJO2.class)));
    }

    @Test
    public void testHashCode() throws Exception {
        Assert.assertTrue(new PropertyAccessor(SimplePOJO.class).hashCode() == new PropertyAccessor(SimplePOJO.class).hashCode());
        Assert.assertFalse(new PropertyAccessor(SimplePOJO.class).hashCode() == new PropertyAccessor(SimplePOJO2.class).hashCode());
    }
}
