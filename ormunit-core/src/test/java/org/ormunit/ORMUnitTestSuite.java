package org.ormunit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.ormunit.entity.EntityAccessorTest;
//import org.ormunit.entity.EntityCommandTest;
import org.ormunit.node.CoreEntityNodeProcessorTest;
import org.ormunit.node.ImportNodeProcessorTest;
import org.ormunit.node.IncludeNodeProcessorTest;
import org.ormunit.node.SimplePOJOTest;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 18:11
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeanUtilsTest.class,
        SimplePOJOTest.class,

        ORMUnitHelperTest.class,
        ORMUnitIntrospectorTest.class,

        EntityAccessorTest.class,

        IncludeNodeProcessorTest.class,
        ImportNodeProcessorTest.class,
        CoreEntityNodeProcessorTest.class,
        //EntityCommandTest.class,
        ORMUnitTest.class,
        TestSetTest.class,
        ORMUnitDataSourceTest.class
})
public class ORMUnitTestSuite {
}
