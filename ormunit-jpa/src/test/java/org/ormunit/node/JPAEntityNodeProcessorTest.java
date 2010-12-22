package org.ormunit.node;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.ORMUnitConfiguration;
import org.ormunit.ORMUnitConfigurationReader;
import org.ormunit.ORMUnitIntrospector;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.command.JPAORMProvider;
import org.ormunit.entity.AttributeAccessEntity;
import org.ormunit.entity.PropertyAccessEntity;
import org.ormunit.exception.ORMUnitFileReadException;

import javax.persistence.EntityManager;
import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 17:56
 */
@RunWith(MockitoJUnitRunner.class)
public class JPAEntityNodeProcessorTest {

    @Mock
    private EntityManager em;


    @Test
    public void testExtractIdType() {
        junit.framework.Assert.assertEquals(int.class, new JPAORMProvider(em).getIdType(AttributeAccessEntity.class));
        junit.framework.Assert.assertEquals(Integer.class, new JPAORMProvider(em).getIdType(PropertyAccessEntity.class));
    }

    @Test
    public void testComplexTypeWithReference() throws ORMUnitFileReadException, IntrospectionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.AttributeAccessEntity\" alias=\"pojo\" /> " +
                "   <pojo complexType=\"ref(1)\"> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        ORMUnitConfiguration result = spy(new ORMUnitConfiguration(new JPAORMProvider(em)));
        new ORMUnitConfigurationReader().read(bais, result);

        AttributeAccessEntity entity = new AttributeAccessEntity();
        Set<EntityReference> references = new HashSet<EntityReference>();
        references.add(new EntityReference(ORMUnitIntrospector.getInspector(entity.getClass()), "complexType", 1));
        verify(result, times(1)).addCommand(eq(new EntityCommand(entity, references)));
    }

    @Test
    public void testComplexTypeWithReferenceSubElement() throws ORMUnitFileReadException, IntrospectionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.AttributeAccessEntity\" alias=\"pojo\" /> " +
                "   <pojo> " +
                "       <complexType> ref(1) </complexType> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        ORMUnitConfiguration result = spy(new ORMUnitConfiguration(new JPAORMProvider(em)));
        new ORMUnitConfigurationReader().read(bais, result);

        AttributeAccessEntity entity = new AttributeAccessEntity();
        Set<EntityReference> references = new HashSet<EntityReference>();
        references.add(new EntityReference(ORMUnitIntrospector.getInspector(entity.getClass()), "complexType", 1));
        verify(result, times(1)).addCommand(eq(new EntityCommand(entity, references)));
    }

}
