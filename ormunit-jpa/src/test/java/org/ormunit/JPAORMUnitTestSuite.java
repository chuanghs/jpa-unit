package org.ormunit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.ormunit.entity.embedded.EmbeddingTest;
import org.ormunit.live.JPAUnitEntitiesWithComposedIdsTest;
import org.ormunit.live.JPAUnitOrmXmlReadTest;
import org.ormunit.live.JPAUnitRealEntitiesTest;
import org.ormunit.node.EntityReferencesTest;
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
        EntityReferencesTest.class,
        JPAUnitOrmXmlReadTest.class,
        AnnotationsEntityInspectorTest.class,
        EntityMappingsEntityInspectorTest.class,
        JPAUnitRealEntitiesTest.class,
        EmbeddingTest.class,
        JPAUnitEntitiesWithComposedIdsTest.class
})
public class JPAORMUnitTestSuite {
}
