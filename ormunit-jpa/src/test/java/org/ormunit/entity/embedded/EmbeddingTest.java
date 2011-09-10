package org.ormunit.entity.embedded;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.JPAORMProvider;
import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.command.EntityCommand;
import org.ormunit.entity.FieldAccessor;
import org.ormunit.exception.FileReadException;

import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: jan
 * Date: 10.09.11
 * Time: 21:56
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmbeddingTest {

    @Mock
    private EntityManager em;

    @Test
    public void testReadFile() throws FileReadException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.embedded.EmbeddableClass\" /> " +
                "   <import class=\"org.ormunit.entity.embedded.EmbeddingEntity\" /> " +
                "   <EmbeddingEntity> " +
                "       <value><embeddedProperty>some string value</embeddedProperty></value> " +
                "   </EmbeddingEntity>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(new JPAORMProvider(em)));
        new ORMUnitPropertiesReader(getClass()).read(bais, result);


        EmbeddingEntity embeddingEntity = new EmbeddingEntity();
        EmbeddableClass value = new EmbeddableClass();
        value.setEmbeddedProperty("some string value");
        embeddingEntity.setValue(value);

        verify(result, times(1)).addCommand(eq(new EntityCommand(embeddingEntity, new FieldAccessor(embeddingEntity.getClass()))));


    }

}
