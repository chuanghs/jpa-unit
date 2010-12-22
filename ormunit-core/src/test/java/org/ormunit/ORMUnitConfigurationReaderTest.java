package org.ormunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.command.StatementCommand;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitFileSyntaxException;
import org.ormunit.node.INodeProcessor;
import org.ormunit.node.ImportNodeProcessor;

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
    ORMUnitConfiguration configuration = new ORMUnitConfiguration(mock(ORMProvider.class));

    @Test
    public void testReadStatements() throws ORMUnitFileReadException, IOException {

        byte[] value = ("<ormunit> " +
                "<statement code=\"this code shouldnt be added\">code0</statement>" +
                "<statement code=\"this code shouldnt be added\"><![CDATA[code1]]></statement>" +
                "<statement code=\"code2\" /><statement code=\"code3\" /></ormunit>").getBytes();
        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader());
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

        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader());
        reader.read(new ByteArrayInputStream(value), configuration);

        verify(reader, times(2)).getNodeProcessor(eq("statements"));

        verifyNoMoreInteractions(configuration);

    }


    @Test
    public void testInvalidClassNames1() throws ORMUnitFileReadException, IOException {

        assertFalse("1com".matches(ImportNodeProcessor.ClassNamePattern));
        assertTrue("com".matches(ImportNodeProcessor.ClassNamePattern));
        assertTrue("com.example.Class".matches(ImportNodeProcessor.ClassNamePattern));
        assertTrue("$com._example.Class".matches(ImportNodeProcessor.ClassNamePattern));
        assertFalse("$com._example.1Class".matches(ImportNodeProcessor.ClassNamePattern));

    }

    @Test(expected = ORMUnitConfigurationException.class)
    public void testInvalidClassNames2() throws ORMUnitFileReadException, IOException {
        byte[] value = "<ormunit><import class=\"$com._example.SomeClass1\" alias=\"sc1\"/><import class=\"1com.example.SomeClass\" alias=\"sc1\"/></ormunit>".getBytes();

        new ORMUnitConfigurationReader()
                .read(new ByteArrayInputStream(value), configuration);

    }

    @Test
    public void testRegisterNodeProcessor() throws ORMUnitFileReadException, IOException {
        byte[] value = "<ormunit><import class=\"$com._example.SomeClass1\" alias=\"sc1\"/><import class=\"com.example.SomeClass\" alias=\"sc2\"/></ormunit>".getBytes();


        ORMUnitConfigurationReader reader = spy(new ORMUnitConfigurationReader());
        reader.read(new ByteArrayInputStream(value), configuration);

        verify(reader, times(1)).registerNodeProcessor(eq("sc1"), any(INodeProcessor.class));
        verify(reader, times(1)).registerNodeProcessor(eq("sc2"), any(INodeProcessor.class));
    }


    @Test(expected = ORMUnitFileSyntaxException.class)
    public void testUnknownNode() throws ORMUnitFileReadException, IOException {
        byte[] value = "<ormunit><someInvalidAndUndUnknownNode/></ormunit>".getBytes();

        new ORMUnitConfigurationReader()
                .read(new ByteArrayInputStream(value), configuration);
    }


    @Test(expected = ORMUnitFileReadException.class)
    public void testInvalidSyntax() throws ORMUnitFileReadException {
        byte[] value = "some non xml content".getBytes();
        new ORMUnitConfigurationReader()
                .read(new ByteArrayInputStream(value), configuration);
    }
}
