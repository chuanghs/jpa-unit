package org.jpaunit;

import org.jpaunit.exception.JPAUnitConfigurationException;
import org.jpaunit.node.TestNodeProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
public class JPAUnitPropertiesTest {

    @Test
    public void testSetProperties(){
        Properties properties =  new Properties();

        String somestrangenode = "somestrangenode";
        properties.setProperty(JPAUnitConfigurationReader.Properties_NodeProcessor_Prefix+ somestrangenode, TestNodeProcessor.class.getCanonicalName());
        JPAUnitConfigurationReader jpaUnitConfigurationReader = new JPAUnitConfigurationReader(properties);

        assertTrue(jpaUnitConfigurationReader.getNodeProcessor(somestrangenode) instanceof TestNodeProcessor);

    }

    @Test(expected = JPAUnitConfigurationException.class)
    public void testSetWrongProperties(){
        Properties properties =  new Properties();

        String somestrangenode = "somestrangenode";
        properties.setProperty(JPAUnitConfigurationReader.Properties_NodeProcessor_Prefix+ somestrangenode, "somestringthataintclassname");
        JPAUnitConfigurationReader jpaUnitConfigurationReader = new JPAUnitConfigurationReader(properties);

    }


    @Test
    public void testDefaultProperties(){
        JPAUnitConfigurationReader jpaUnitConfigurationReader = new JPAUnitConfigurationReader();

        assertNotNull(jpaUnitConfigurationReader.getNodeProcessor("statement"));
        assertNotNull(jpaUnitConfigurationReader.getNodeProcessor("import"));

    }

}
