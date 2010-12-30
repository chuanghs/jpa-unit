package org.ormunit;

import org.mockito.Mockito;
import org.mockito.Spy;
import org.ormunit.command.StatementCommand;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitFileSyntaxException;
import org.ormunit.node.EntityNodeProcessor;
import org.ormunit.node.INodeProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 15:40
 */
@RunWith(MockitoJUnitRunner.class)
public class ORMUnitConfigurationReaderTest {


    @Spy
    ORMUnitConfiguration configuration = new ORMUnitConfiguration();

    @Test
    public void testReadStatements() throws ORMUnitFileReadException, IOException {

        byte[] value = ("<ormunit> " +
                "<statement code=\"this code shouldnt be added\">code0</statement>" +
                "<statement code=\"this code shouldnt be added\"><![CDATA[code1]]></statement>" +
                "<statement code=\"code2\" /><statement code=\"code3\" /></ormunit>").getBytes();
        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader(Mockito.mock(ORMProvider.class)));
        reader.read(new ByteArrayInputStream(value), configuration);

        verify(reader, times(4)).getNodeProcessor(eq("statement"));

        verify(configuration, times(1)).addCommand(Matchers.eq(new StatementCommand("code0")));
        verify(configuration, times(1)).addCommand(Matchers.eq(new StatementCommand("code1")));
        verify(configuration, times(1)).addCommand(Matchers.eq(new StatementCommand("code2")));
        verify(configuration, times(1)).addCommand(Matchers.eq(new StatementCommand("code3")));

        verifyNoMoreInteractions(configuration);

    }

    @Test(expected = ORMUnitFileSyntaxException.class)
    public void testStatementWith2Children() throws ORMUnitFileReadException, IOException {

        byte[] value = "<ormunit> <statement code=\"this code shouldnt be added\"><![CDATA[code1]]><somesubelem /></statement></ormunit>".getBytes();

        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader(Mockito.mock(ORMProvider.class)));
        reader.read(new ByteArrayInputStream(value), configuration);

        verify(reader, times(2)).getNodeProcessor(eq("statements"));

        verifyNoMoreInteractions(configuration);

    }


    @Test
    public void testImports() throws ORMUnitFileReadException, IOException {
        byte[] value = ("<ormunit> " +
                "<import class=\"com.example.SomeClass1\" alias=\"sc1\"/><import class=\"com.example.SomeClass\"/></ormunit>").getBytes();


        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader(Mockito.mock(ORMProvider.class)));
        reader.read(new ByteArrayInputStream(value), configuration);

        verify(reader, times(2)).getNodeProcessor(eq("import"));

        verify(reader, times(1)).registerNodeProcessor(eq("sc1"), any(EntityNodeProcessor.class));
        verify(reader, times(1)).registerNodeProcessor(eq("SomeClass"), any(EntityNodeProcessor.class));

        verify(configuration).addImport(eq("com.example.SomeClass1"), eq("sc1"));
        verify(configuration).addImport(eq("com.example.SomeClass"), eq("SomeClass"));

        verifyNoMoreInteractions(configuration);
    }

    @Test(expected = ORMUnitConfigurationException.class)
    public void testAmbiguousAlias() throws ORMUnitFileReadException, IOException {
        byte[] value = ("<ormunit>" +
                "       <import class=\"com.example.SomeClass1\" alias=\"sc1\"/>" +
                "       <import class=\"com.example.SomeClass\" alias=\"sc1\"/>" +
                "</ormunit>").getBytes();
        new ORMUnitConfigurationReader(Mockito.mock(ORMProvider.class))
                .read(new ByteArrayInputStream(value), configuration);
    }


    @Test
    public void testInvalidClassNames1() throws ORMUnitFileReadException, IOException {

        assertFalse("1com".matches(ORMUnitConfiguration.ClassNamePattern));
        assertTrue("com".matches(ORMUnitConfiguration.ClassNamePattern));
        assertTrue("com.example.Class".matches(ORMUnitConfiguration.ClassNamePattern));
        assertTrue("$com._example.Class".matches(ORMUnitConfiguration.ClassNamePattern));
        assertFalse("$com._example.1Class".matches(ORMUnitConfiguration.ClassNamePattern));

    }

    @Test(expected = ORMUnitConfigurationException.class)
    public void testInvalidClassNames2() throws ORMUnitFileReadException, IOException {
        byte[] value = "<ormunit><import class=\"$com._example.SomeClass1\" alias=\"sc1\"/><import class=\"1com.example.SomeClass\" alias=\"sc1\"/></ormunit>".getBytes();

        new ORMUnitConfigurationReader(Mockito.mock(ORMProvider.class))
                   .read(new ByteArrayInputStream(value), configuration);

    }

    @Test
    public void testRegisterNodeProcessor() throws ORMUnitFileReadException, IOException {
        byte[] value = "<ormunit><import class=\"$com._example.SomeClass1\" alias=\"sc1\"/><import class=\"com.example.SomeClass\" alias=\"sc2\"/></ormunit>".getBytes();


        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader(Mockito.mock(ORMProvider.class)));
        reader.read(new ByteArrayInputStream(value), configuration);

        verify(reader, times(1)).registerNodeProcessor(eq("sc1"), any(INodeProcessor.class));
        verify(reader, times(1)).registerNodeProcessor(eq("sc2"), any(INodeProcessor.class));
    }


    @Test(expected = ORMUnitFileSyntaxException.class)
    public void testUnknownNode() throws ORMUnitFileReadException, IOException {
        byte[] value = "<ormunit><someInvalidAndUndUnknownNode/></ormunit>".getBytes();

        new ORMUnitConfigurationReader(Mockito.mock(ORMProvider.class))
                .read(new ByteArrayInputStream(value), configuration);
    }


    @Test(expected = ORMUnitFileReadException.class)
    public void testInvalidSyntax() throws ORMUnitFileReadException {
        byte[] value = "some non xml content".getBytes();
        new ORMUnitConfigurationReader(Mockito.mock(ORMProvider.class))
                   .read(new ByteArrayInputStream(value), configuration);
    }
}
