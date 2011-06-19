package org.ormunit.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.ormunit.ORMProvider;
import org.ormunit.TestSet;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.command.TestSetCommand;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 17.02.11
 * Time: 11:58
 */

@RunWith(JUnit4.class)
public class EntityCommandTest {


    @Test
    public void testReferences() {

        SimplePOJO2 simplePOJO2 = new SimplePOJO2();
        EntityCommand entityCommand1 = new EntityCommand("pojo2", simplePOJO2, new PropertyAccessor(simplePOJO2.getClass()));

        SimplePOJO simplePOJO = new SimplePOJO();
        Set<EntityReference> references = new HashSet<EntityReference>();
        references.add(new EntityReference("complexType", "pojo2", EntityReference.Type.ORMUNIT));
        EntityCommand entityCommand2 = new EntityCommand("pojo", simplePOJO, new PropertyAccessor(simplePOJO.getClass()), references);

        ORMProvider provider = mock(ORMProvider.class);

        TestSet testSet = new TestSet(provider);
        testSet.addCommand(entityCommand1);
        testSet.addCommand(entityCommand2);

        testSet.execute();


        InOrder inOrder = inOrder(provider);
        inOrder.verify(provider).entity(same(simplePOJO2));

        SimplePOJO entity2 = new SimplePOJO();
        entity2.setComplexType(simplePOJO2);
        inOrder.verify(provider).entity(eq(entity2));
    }


    @Test
    public void testReferencesFromIncludedTestSet() {

        SimplePOJO2 simplePOJO2 = new SimplePOJO2();
        EntityCommand entityCommand1 = new EntityCommand("pojo2", simplePOJO2, new PropertyAccessor(simplePOJO2.getClass()));

        SimplePOJO simplePOJO = new SimplePOJO();
        Set<EntityReference> references = new HashSet<EntityReference>();
        references.add(new EntityReference("complexType", "pojo2", EntityReference.Type.ORMUNIT));
        EntityCommand entityCommand2 = new EntityCommand("pojo", simplePOJO, new PropertyAccessor(simplePOJO.getClass()), references);

        ORMProvider provider = mock(ORMProvider.class);



        TestSet rootTestSet = new TestSet(provider);

        TestSet includedTestSet = new TestSet(rootTestSet);
        includedTestSet.addCommand(entityCommand1);
        rootTestSet.addCommand(new TestSetCommand(includedTestSet));

        rootTestSet.addCommand(entityCommand2);


        rootTestSet.execute();

        InOrder inOrder = inOrder(provider);
        inOrder.verify(provider).entity(same(simplePOJO2));

        SimplePOJO entity2 = new SimplePOJO();
        entity2.setComplexType(simplePOJO2);
        inOrder.verify(provider).entity(eq(entity2));

    }
}
