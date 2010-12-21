package org.ormunit;

import org.ormunit.node.EntityNodeProcessorTest;
import org.ormunit.node.SimplePOJOTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 18:11
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SimplePOJOTest.class,
        ORMUnitPropertiesTest.class,
        ORMUnitConfigurationReaderTest.class,
        EntityNodeProcessorTest.class,
        ORMUnitConfigurationTest.class})
public class ORMUnitTestSuite {
}
