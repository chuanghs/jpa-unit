package org.jpaunit.node;

import junit.framework.Assert;
import org.jpaunit.entity.SimplePOJO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 21:30
 */
@RunWith(MockitoJUnitRunner.class)
public class SimplePOJOTest {

    @Test
    public void testEquals(){

        Assert.assertEquals(new SimplePOJO(), new SimplePOJO());
        Assert.assertEquals(new SimplePOJO(), new SimplePOJO());
        Assert.assertEquals(new SimplePOJO().hashCode(), new SimplePOJO().hashCode());

    }
}
