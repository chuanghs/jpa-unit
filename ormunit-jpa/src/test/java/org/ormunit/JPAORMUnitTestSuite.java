package org.ormunit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.ormunit.live.JPAUnitSimpleTest;
import org.ormunit.live.JPAUnitSimpleTest2;
import org.ormunit.node.JPAEntityNodeProcessorTest;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 19:14
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        JPAORMProviderTest.class,
        JPAEntityNodeProcessorTest.class,
        JPAUnitSimpleTest.class,
        JPAUnitSimpleTest2.class
})
public class JPAORMUnitTestSuite {
}
