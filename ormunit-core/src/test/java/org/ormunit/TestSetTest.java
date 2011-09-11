package org.ormunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.StatementCommand;
import org.ormunit.command.TestSetCommand;
import org.ormunit.node.entity.accessor.PropertyAccessor;
import org.ormunit.entity.SimplePOJO;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 15:31
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSetTest {

    @Test
    public void testExecutionOrder() {
        ORMProvider visitor = mock(ORMProvider.class);
        TestSet testSet = new TestSet(visitor);

        SimplePOJO simplePOJO1 = new SimplePOJO();
        SimplePOJO simplePOJO2 = new SimplePOJO();
        SimplePOJO simplePOJO3 = new SimplePOJO();

        testSet.addCommand(new StatementCommand("statement1"));
        testSet.addCommand(new EntityCommand(simplePOJO1, new PropertyAccessor(simplePOJO1.getClass())));
        testSet.addCommand(new StatementCommand("statement2"));
        testSet.addCommand(new EntityCommand(simplePOJO2, new PropertyAccessor(simplePOJO2.getClass())));

        TestSet includedTestSet = new TestSet(visitor);
        includedTestSet.addCommand(new EntityCommand(simplePOJO3, new PropertyAccessor(simplePOJO3.getClass())));
        testSet.addCommand(new TestSetCommand(includedTestSet));


        testSet.execute();

        InOrder inOrder = inOrder(visitor);

        inOrder.verify(visitor).statement(eq("statement1"));
        inOrder.verify(visitor).entity(same(simplePOJO1));
        inOrder.verify(visitor).statement(eq("statement2"));
        inOrder.verify(visitor).entity(same(simplePOJO2));

        inOrder.verify(visitor).entity(same(simplePOJO3));
    }
}
