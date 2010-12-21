package org.ormunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.node.FakeNodeProcessor;

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

    @Test
    public void testSetProperties() {
        Properties properties = new Properties();

        String somestrangenode = "somestrangenode";
        properties.setProperty(ORMUnitConfigurationReader.Properties_NodeProcessor_Prefix + somestrangenode, FakeNodeProcessor.class.getCanonicalName());
        ORMUnitConfigurationReader jpaUnitConfigurationReader = new ORMUnitConfigurationReader(properties);

        assertTrue(jpaUnitConfigurationReader.getNodeProcessor(somestrangenode) instanceof FakeNodeProcessor);

    }

    @Test(expected = ORMUnitConfigurationException.class)
    public void testSetWrongProperties() {
        Properties properties = new Properties();

        String somestrangenode = "somestrangenode";
        properties.setProperty(ORMUnitConfigurationReader.Properties_NodeProcessor_Prefix + somestrangenode, "somestringthataintclassname");
        ORMUnitConfigurationReader jpaUnitConfigurationReader = new ORMUnitConfigurationReader(properties);

    }


    @Test
    public void testDefaultProperties() {
        ORMUnitConfigurationReader jpaUnitConfigurationReader = new ORMUnitConfigurationReader();

        assertNotNull(jpaUnitConfigurationReader.getNodeProcessor("statement"));
        assertNotNull(jpaUnitConfigurationReader.getNodeProcessor("import"));

    }

}
