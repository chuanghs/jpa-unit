package org.ormunit.node;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitConfiguration;
import org.ormunit.ORMUnitConfigurationReader;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitFileSyntaxException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 11:26
 */

@RunWith(MockitoJUnitRunner.class)
public class ImportNodeProcessorTest {


    @Spy
    ORMUnitConfiguration configuration = new ORMUnitConfiguration(mock(ORMProvider.class));

    @Test(expected = ORMUnitConfigurationException.class)
    public void testImportInvalidClassName() throws ORMUnitFileReadException {
        byte[] value = ("<ormunit> " +
                "<import class=\"1some.invalid.0ClassName\" alias=\"some alias\" />" +
                "</ormunit>").getBytes();
        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader(getClass()));
        reader.read(new ByteArrayInputStream(value), configuration);
    }

    @Test(expected = ORMUnitFileSyntaxException.class)
    public void testImportInvalidImportSyntax() throws ORMUnitFileReadException {
        byte[] value = ("<ormunit> " +
                "<import alias=\"some alias\" />" +
                "</ormunit>").getBytes();
        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader(getClass()));
        reader.read(new ByteArrayInputStream(value), configuration);
    }

    @Test
    public void testImports() throws ORMUnitFileReadException, IOException {
        byte[] value = ("<ormunit> " +
                "<import class=\"com.example.SomeClass1\" alias=\"sc1\"/>" +
                "<import class=\"com.example.SomeClass\"/></ormunit>").getBytes();


        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader(getClass()));
        reader.read(new ByteArrayInputStream(value), configuration);

        verify(reader, times(2)).getNodeProcessor(eq("import"));

        verify(reader, times(1)).registerNodeProcessor(eq("sc1"), any(EntityNodeProcessor.class));
        verify(reader, times(1)).registerNodeProcessor(eq("SomeClass"), any(EntityNodeProcessor.class));

        verifyNoMoreInteractions(configuration);
    }

    @Test(expected = ORMUnitConfigurationException.class)
    public void testAmbiguousAlias() throws ORMUnitFileReadException, IOException {
        byte[] value = ("<ormunit>" +
                "       <import class=\"com.example.SomeClass1\" alias=\"sc1\"/>" +
                "       <import class=\"com.example.SomeClass\" alias=\"sc1\"/>" +
                "</ormunit>").getBytes();
        new ORMUnitConfigurationReader(getClass())
                .read(new ByteArrayInputStream(value), configuration);
    }

}
