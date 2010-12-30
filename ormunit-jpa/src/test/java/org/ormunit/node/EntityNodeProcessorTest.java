package org.ormunit.node;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.ORMUnitConfiguration;
import org.ormunit.ORMUnitConfigurationReader;
import org.ormunit.ORMUnitHelper;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.command.JPAORMProvider;
import org.ormunit.entity.AttributeAccessEntity;
import org.ormunit.entity.PropertyAccessEntity;
import org.ormunit.exception.ORMUnitFileReadException;

import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 17:56
 */
@RunWith(JUnit4.class)
public class EntityNodeProcessorTest {

    @Test
    public void testReferencePattern() {
        Assert.assertFalse("ref()".matches(EntityNodeProcessor.ReferencePattern));

        Assert.assertTrue("ref( )".matches(EntityNodeProcessor.ReferencePattern));
        Assert.assertTrue("ref(1)".matches(EntityNodeProcessor.ReferencePattern));
        Assert.assertTrue("ref(someStringReference)".matches(EntityNodeProcessor.ReferencePattern));
    }

    @Test
    public void testExtractIdType() {
        junit.framework.Assert.assertEquals(int.class, new JPAORMProvider().getIdType(AttributeAccessEntity.class));
        junit.framework.Assert.assertEquals(Integer.class, new JPAORMProvider().getIdType(PropertyAccessEntity.class));
    }

    @Test
    public void testComplexTypeWithReference() throws ORMUnitFileReadException, IntrospectionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.AttributeAccessEntity\" alias=\"pojo\" /> " +
                "   <pojo complexType=\"ref(1)\"> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        ORMUnitConfiguration result = spy(new ORMUnitConfiguration());
        new ORMUnitConfigurationReader(new JPAORMProvider()).read(bais, result);

        AttributeAccessEntity entity = new AttributeAccessEntity();
        Set<EntityReference> references = new HashSet<EntityReference>();
        references.add(new EntityReference(entity, "complexType", 1));
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

        ORMUnitConfiguration result = spy(new ORMUnitConfiguration());
        new ORMUnitConfigurationReader(new JPAORMProvider()).read(bais, result);

        AttributeAccessEntity entity = new AttributeAccessEntity();
        Set<EntityReference> references = new HashSet<EntityReference>();
        references.add(new EntityReference(entity, "complexType", 1));
        verify(result, times(1)).addCommand(eq(new EntityCommand(entity, references)));
    }

}
