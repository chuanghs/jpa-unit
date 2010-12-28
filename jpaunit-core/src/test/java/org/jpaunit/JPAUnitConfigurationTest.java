package org.jpaunit;

import org.jpaunit.command.JPAUnitCommandVisitor;
import org.jpaunit.node.SimplePOJO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 15:31
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class JPAUnitConfigurationTest {

    @Test
    public void testExecutionOrder() {
        JPAUnitConfiguration jpaUnitConfiguration = new JPAUnitConfiguration();

        SimplePOJO simplePOJO1 = new SimplePOJO();
        SimplePOJO simplePOJO2 = new SimplePOJO();

        jpaUnitConfiguration.addStatement("statement1");
        jpaUnitConfiguration.addEntity(simplePOJO1);
        jpaUnitConfiguration.addStatement("statement2");
        jpaUnitConfiguration.addEntity(simplePOJO2);

        EntityManager entityManager = mock(EntityManager.class);

        doReturn(mock(Query.class)).when(entityManager).createNativeQuery(anyString());

        JPAUnitCommandVisitor visitor = new JPAUnitCommandVisitor(entityManager);
        jpaUnitConfiguration.visit(visitor);

        InOrder inOrder = inOrder(entityManager);

        inOrder.verify(entityManager).createNativeQuery(eq("statement1"));
        inOrder.verify(entityManager).persist(eq(simplePOJO1));
        inOrder.verify(entityManager).createNativeQuery(eq("statement2"));
        inOrder.verify(entityManager).persist(eq(simplePOJO2));
    }
}
