package org.jpaunit;

import org.jpaunit.node.EntityNodeProcessorTest;
import org.jpaunit.node.SimplePOJOTest;
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
        JPAUnitPropertiesTest.class,
        JPAUnitConfigurationReaderTest.class,
        EntityNodeProcessorTest.class,
        JPAUnitConfigurationTest.class})
public class JPAUnitTestSuite {
}
