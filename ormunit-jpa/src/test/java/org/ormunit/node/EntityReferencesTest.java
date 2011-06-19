package org.ormunit.node;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.JPAORMProvider;
import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.entity.FieldAccessEntity;
import org.ormunit.entity.FieldAccessor;
import org.ormunit.entity.PropertyAccessEntity;
import org.ormunit.exception.ORMUnitFileReadException;

import javax.persistence.EntityManager;
import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.util.HashSet;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 21.04.11
 * Time: 20:34
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityReferencesTest {

    @Mock
    private EntityManager em;


    @Test
    public void testComplexTypeWithReferenceSubElement() throws ORMUnitFileReadException, IntrospectionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.PropertyAccessEntity\" /> " +
                "   <import class=\"org.ormunit.entity.FieldAccessEntity\" /> " +
                "   <FieldAccessEntity integerValue=\"1\"> " +
                "       <complexType><PropertyAccessEntity><id>1</id></PropertyAccessEntity></complexType> " +
                "   </FieldAccessEntity>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(new JPAORMProvider(em)));
        new ORMUnitPropertiesReader(getClass()).read(bais, result);

        FieldAccessEntity entity = new FieldAccessEntity();
        entity.setIntegerValue(1);

        PropertyAccessEntity propertyAccessEntity = new PropertyAccessEntity();
        propertyAccessEntity.setId(1);
        entity.setComplexType(propertyAccessEntity);

        verify(result, times(1)).addCommand(eq(new EntityCommand(null, propertyAccessEntity, new FieldAccessor(propertyAccessEntity.getClass()), new HashSet<EntityReference>())));
        verify(result, times(1)).addCommand(eq(new EntityCommand(null, entity, new FieldAccessor(entity.getClass()), new HashSet<EntityReference>())));


        result.execute();

        FieldAccessEntity persistedEntity = new FieldAccessEntity();
        persistedEntity.setIntegerValue(1);

        PropertyAccessEntity persistedPropertyAccessEntity = new PropertyAccessEntity();
        persistedEntity.setComplexType(persistedPropertyAccessEntity);

        verify(em, times(1)).persist(eq(persistedPropertyAccessEntity));
        verify(em, times(1)).persist(eq(persistedEntity));
    }

}

