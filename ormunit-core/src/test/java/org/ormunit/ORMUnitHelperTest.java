package org.ormunit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.node.CoreEntityNodeProcessorTest;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 11:40
 */

@RunWith(JUnit4.class)
public class ORMUnitHelperTest {

    @Test
    public void testReadProperties() throws IOException {
        Properties properties = ORMUnitHelper.readOrmUnitProperties(CoreEntityNodeProcessorTest.class);

        Assert.assertEquals("toplevelvalue", properties.getProperty("toplevelproperty"));
        Assert.assertEquals("middlelevelvalue_replaced", properties.getProperty("middlelevelproperty"));
        Assert.assertEquals("lowerlevelvalue_replaced", properties.getProperty("lowerlevelproperty"));
    }

}
