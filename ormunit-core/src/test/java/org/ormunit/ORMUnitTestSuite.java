package org.ormunit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.ormunit.node.EntityNodeProcessorTest;
import org.ormunit.node.SimplePOJOTest;

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
        ORMUnitConfigurationTest.class,
        ORMUnitHelperTest.class})
public class ORMUnitTestSuite {
}
