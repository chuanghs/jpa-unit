package org.ormunit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.entity.SimplePOJO;
import org.ormunit.exception.ConvertionException;
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


    @Test
    public void testConvertions() throws ConvertionException {
        Assert.assertEquals(1, ORMUnitHelper.convert(Integer.class, "1"));
        Assert.assertSame(true, ORMUnitHelper.convert(boolean.class, "true"));
        Assert.assertEquals(1.234f, ORMUnitHelper.convert(float.class, "1.234"));
        Assert.assertEquals(new Double(1.234d), ORMUnitHelper.convert(Double.class, "1.234"));

        Assert.assertEquals('c', ORMUnitHelper.convert(char.class, "c"));
        Assert.assertEquals((byte) -1, ORMUnitHelper.convert(byte.class, "-1"));
    }

    @Test
    public void testConvertionException() {
        try {
            ORMUnitHelper.convert(Integer.class, "somenotintegerstring");
            junit.framework.Assert.fail();
        } catch (ConvertionException e) {
        }

        try {
            ORMUnitHelper.convert(Byte.class, "someweirdstring");
            junit.framework.Assert.fail();
        } catch (ConvertionException e) {
        }

        try {
            ORMUnitHelper.convert(boolean.class, "definitlynotboolean");
            junit.framework.Assert.fail();
        } catch (ConvertionException e) {
        }
    }

    @Test(expected = ConvertionException.class)
    public void testUnsupporterType() throws ConvertionException {
        ORMUnitHelper.convert(SimplePOJO.class, "some value that should cause Helper to throw an exception");
    }

}
