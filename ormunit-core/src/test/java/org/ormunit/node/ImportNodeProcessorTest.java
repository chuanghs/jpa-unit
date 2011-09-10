package org.ormunit.node;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.exception.ConfigurationException;
import org.ormunit.exception.FileReadException;
import org.ormunit.exception.FileSyntaxException;

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
    TestSet testSet = new TestSet(mock(ORMProvider.class));

    @Test(expected = ConfigurationException.class)
    public void testImportInvalidClassName() throws FileReadException {
        byte[] value = ("<ormunit> " +
                "<import class=\"1some.invalid.0ClassName\" alias=\"some alias\" />" +
                "</ormunit>").getBytes();
        ORMUnitPropertiesReader reader = spy(new ORMUnitPropertiesReader(getClass()));
        reader.read(new ByteArrayInputStream(value), testSet);
    }

    @Test(expected = FileSyntaxException.class)
    public void testImportInvalidImportSyntax() throws FileReadException {
        byte[] value = ("<ormunit> " +
                "<import alias=\"some alias\" />" +
                "</ormunit>").getBytes();
        ORMUnitPropertiesReader reader = spy(new ORMUnitPropertiesReader(getClass()));
        reader.read(new ByteArrayInputStream(value), testSet);
    }

    @Test
    public void testImports() throws FileReadException, IOException {
        byte[] value = ("<ormunit> " +
                "<import class=\"com.example.SomeClass1\" alias=\"sc1\"/>" +
                "<import class=\"com.example.SomeClass\"/></ormunit>").getBytes();


        ORMUnitPropertiesReader reader = spy(new ORMUnitPropertiesReader(getClass()));
        reader.read(new ByteArrayInputStream(value), testSet);

        verify(testSet, times(2)).getNodeProcessor(eq("import"));

        verify(testSet, times(1)).registerNodeProcessor(eq("sc1"), any(EntityNodeProcessor.class));
        verify(testSet, times(1)).registerNodeProcessor(eq("SomeClass"), any(EntityNodeProcessor.class));

        //verifyNoMoreInteractions(testSet);
    }

    @Test(expected = ConfigurationException.class)
    public void testAmbiguousAlias() throws FileReadException, IOException {
        byte[] value = ("<ormunit>" +
                "       <import class=\"com.example.SomeClass1\" alias=\"sc1\"/>" +
                "       <import class=\"com.example.SomeClass\" alias=\"sc1\"/>" +
                "</ormunit>").getBytes();
        new ORMUnitPropertiesReader(getClass())
                .read(new ByteArrayInputStream(value), testSet);
    }

}
