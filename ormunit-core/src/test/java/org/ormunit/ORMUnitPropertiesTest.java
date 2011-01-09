package org.ormunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.node.FakeNodeProcessor;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 17:28
 */
@RunWith(MockitoJUnitRunner.class)
public class ORMUnitPropertiesTest {


    @Test(expected = ORMUnitConfigurationException.class)
    public void testSetWrongProperties() throws ORMUnitFileReadException {
        Properties properties = new Properties();

        String somestrangenode = "somestrangenode";
        properties.setProperty(ORMUnit.Properties_NodeProcessor_Prefix + somestrangenode, "somestringthataintclassname");
        ORMUnit jpaUnitConfigurationReader = new ORMUnit(properties);

        jpaUnitConfigurationReader.read(Mockito.mock(InputStream.class), new ORMUnitTestSet(Mockito.mock(ORMProvider.class)));

    }


}
