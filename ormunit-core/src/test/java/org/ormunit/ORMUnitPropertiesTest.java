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
        properties.setProperty(ORMUnit.Properties_NodeProcessor_Prefix + somestrangenode, FakeNodeProcessor.class.getCanonicalName());
        ORMUnit jpaUnitConfigurationReader = new ORMUnit(properties);

        assertTrue(jpaUnitConfigurationReader.getNodeProcessor(somestrangenode) instanceof FakeNodeProcessor);

    }

    @Test(expected = ORMUnitConfigurationException.class)
    public void testSetWrongProperties() {
        Properties properties = new Properties();

        String somestrangenode = "somestrangenode";
        properties.setProperty(ORMUnit.Properties_NodeProcessor_Prefix + somestrangenode, "somestringthataintclassname");
        ORMUnit jpaUnitConfigurationReader = new ORMUnit(properties);

    }


    @Test
    public void testDefaultProperties() {
        ORMUnit jpaUnitConfigurationReader = new ORMUnit(getClass());

        assertNotNull(jpaUnitConfigurationReader.getNodeProcessor("statement"));
        assertNotNull(jpaUnitConfigurationReader.getNodeProcessor("import"));

    }

}
