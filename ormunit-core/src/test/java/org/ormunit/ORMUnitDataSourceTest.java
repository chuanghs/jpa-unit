package org.ormunit;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.ORMUnit;
import org.ormunit.exception.ORMUnitConfigurationException;

import javax.naming.NamingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 02.01.11
 * Time: 21:30
 */
@RunWith(JUnit4.class)
public class ORMUnitDataSourceTest {

    @Test
    public void test() throws IOException, NamingException {

        InputStream inputStream = new ByteArrayInputStream((
                "ormunit.datasources=ds1, ds2 \n" +
                        "ormunit.datasources.default = ds1\n" +

                        "ds1.javax.persistence.jdbc.driver=org.apache.derby.jdbc.Driver40 \n" +
                        "ds1.javax.persistence.jdbc.url=jdbc:derby:memory:ormunit1;create=true \n" +
                        "ds1.javax.persistence.jdbc.user=sa \n" +
                        "ds1.javax.persistence.jdbc.password= \n" +

                        "ds2.javax.persistence.jdbc.driver=org.apache.derby.jdbc.Driver40 \n" +
                        "ds2.javax.persistence.jdbc.url=jdbc:derby:memory:ormunit2 \n")
                .getBytes());
        Properties properties = new Properties();
        properties.load(inputStream);

        ORMUnit ormUnit = new ORMUnit(getClass(), properties);

        properties = ormUnit.getDefaultDataSourceProperties();


        Assert.assertEquals("org.apache.derby.jdbc.Driver40", properties.getProperty("javax.persistence.jdbc.driver"));
        Assert.assertEquals("jdbc:derby:memory:ormunit1;create=true", properties.getProperty("javax.persistence.jdbc.url"));
        Assert.assertEquals("sa", properties.getProperty("javax.persistence.jdbc.user"));
        Assert.assertEquals("", properties.getProperty("javax.persistence.jdbc.password"));

    }

    @Test
    public void testNoDefaultDSProperties() throws IOException {

        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        Properties properties = new Properties();
        properties.load(inputStream);

        ORMUnit ormUnit = new ORMUnit(getClass(), properties);

        try {
            properties = ormUnit.getDefaultDataSourceProperties();
            Assert.fail("not default datasource properties set, so exception should be thrown");
        } catch (ORMUnitConfigurationException e) {
        }


    }

}
