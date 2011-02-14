package org.ormunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.StatementCommand;
import org.ormunit.entity.PropertyAccessor;
import org.ormunit.entity.SimplePOJO;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 15:31
 */
@RunWith(MockitoJUnitRunner.class)
public class ORMUnitTestSetTest {

    @Test
    public void testExecutionOrder() {
        ORMProvider visitor = mock(ORMProvider.class);
        ORMUnitTestSet jpaUnitConfiguration = new ORMUnitTestSet(visitor);

        SimplePOJO simplePOJO1 = new SimplePOJO();
        SimplePOJO simplePOJO2 = new SimplePOJO();

        jpaUnitConfiguration.addCommand(new StatementCommand("statement1"));
        jpaUnitConfiguration.addCommand(new EntityCommand(simplePOJO1, new PropertyAccessor(simplePOJO1.getClass())));
        jpaUnitConfiguration.addCommand(new StatementCommand("statement2"));
        jpaUnitConfiguration.addCommand(new EntityCommand(simplePOJO2, new PropertyAccessor(simplePOJO2.getClass())));


        jpaUnitConfiguration.execute();

        InOrder inOrder = inOrder(visitor);

        inOrder.verify(visitor).statement(eq("statement1"));
        inOrder.verify(visitor).entity(eq(simplePOJO1));
        inOrder.verify(visitor).statement(eq("statement2"));
        inOrder.verify(visitor).entity(eq(simplePOJO2));
    }
}
