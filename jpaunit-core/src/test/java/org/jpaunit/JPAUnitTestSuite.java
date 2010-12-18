package org.jpaunit;

import org.jpaunit.node.EntityNodeProcessorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 18:11
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({JPAUnitPropertiesTest.class, JPAUnitConfigurationReaderTest.class, EntityNodeProcessorTest.class})
public class JPAUnitTestSuite {
}
