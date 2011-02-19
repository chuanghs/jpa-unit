package org.ormunit.node;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.entity.SimplePOJO;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 21:30
 */
@RunWith(MockitoJUnitRunner.class)
public class SimplePOJOTest {

    @Test
    public void testEquals() {

        Assert.assertEquals(new SimplePOJO(), new SimplePOJO());
        Assert.assertEquals(new SimplePOJO(), new SimplePOJO());
        Assert.assertEquals(new SimplePOJO().hashCode(), new SimplePOJO().hashCode());

    }
}
