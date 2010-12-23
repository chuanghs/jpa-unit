package org.jpaunit;

import org.jpaunit.exception.JPAUnitConfigurationException;
import org.jpaunit.exception.JPAUnitFileReadException;
import org.jpaunit.exception.JPAUnitFileSyntaxException;
import org.jpaunit.node.EntityNodeProcessor;
import org.jpaunit.node.INodeProcessor;
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
public class JPAUnitConfigurationReaderTest {


    @Test
    public void testReadStatements() throws JPAUnitFileReadException, IOException {

        byte[] value = "<jpaunit><statement code=\"this code shouldnt be added\"><![CDATA[code1]]></statement><statement code=\"code2\" /><statement code=\"code3\" /></jpaunit>".getBytes();
        JPAUnitConfiguration mock = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), mock);

        verify(mock, times(3)).getNodeProcessor(eq("statement"));

        verify(mock, times(1)).addStatement(Matchers.eq("code1"));
        verify(mock, times(1)).addStatement(Matchers.eq("code2"));
        verify(mock, times(1)).addStatement(Matchers.eq("code3"));

        verifyNoMoreInteractions(mock);

    }

    @Test(expected = JPAUnitFileSyntaxException.class)
    public void testStatementWith2Children() throws JPAUnitFileReadException, IOException {

        byte[] value = "<jpaunit><statement code=\"this code shouldnt be added\"><![CDATA[code1]]><somesubelem /></statement></jpaunit>".getBytes();
        JPAUnitConfiguration mock = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), mock);

        verify(mock, times(2)).getNodeProcessor(eq("statements"));

        verifyNoMoreInteractions(mock);

    }


    @Test
    public void testImports() throws JPAUnitFileReadException, IOException {
        byte[] value = "<jpaunit><import class=\"com.example.SomeClass1\" alias=\"sc1\"/><import class=\"com.example.SomeClass\"/></jpaunit>".getBytes();

        JPAUnitConfiguration mock = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), mock);

        verify(mock, times(2)).getNodeProcessor(eq("import"));

        verify(mock, times(1)).registerNodeProcessor(eq("sc1"), any(EntityNodeProcessor.class));
        verify(mock, times(1)).registerNodeProcessor(eq("SomeClass"), any(EntityNodeProcessor.class));

        verify(mock).addImport(eq("com.example.SomeClass1"), eq("sc1"));
        verify(mock).addImport(eq("com.example.SomeClass"), eq("SomeClass"));

        verifyNoMoreInteractions(mock);
    }

    @Test(expected = JPAUnitConfigurationException.class)
    public void testAmbiguousAlias() throws JPAUnitFileReadException, IOException {
        byte[] value = "<jpaunit><import class=\"com.example.SomeClass1\" alias=\"sc1\"/><import class=\"com.example.SomeClass\" alias=\"sc1\"/></jpaunit>".getBytes();
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), new JPAUnitConfiguration());
    }


    @Test
    public void testInvalidClassNames1() throws JPAUnitFileReadException, IOException {

        assertFalse("1com".matches(JPAUnitConfiguration.ClassNamePattern));
        assertTrue("com".matches(JPAUnitConfiguration.ClassNamePattern));
        assertTrue("com.example.Class".matches(JPAUnitConfiguration.ClassNamePattern));
        assertTrue("$com._example.Class".matches(JPAUnitConfiguration.ClassNamePattern));
        assertFalse("$com._example.1Class".matches(JPAUnitConfiguration.ClassNamePattern));

    }

    @Test(expected = JPAUnitConfigurationException.class)
    public void testInvalidClassNames2() throws JPAUnitFileReadException, IOException {
        byte[] value = "<jpaunit><import class=\"$com._example.SomeClass1\" alias=\"sc1\"/><import class=\"1com.example.SomeClass\" alias=\"sc1\"/></jpaunit>".getBytes();
        JPAUnitConfiguration conf = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), conf);

    }

    @Test
    public void testRegisterNodeProcessor() throws JPAUnitFileReadException, IOException {
        byte[] value = "<jpaunit><import class=\"$com._example.SomeClass1\" alias=\"sc1\"/><import class=\"com.example.SomeClass\" alias=\"sc2\"/></jpaunit>".getBytes();

        JPAUnitConfiguration conf = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), conf);

        verify(conf, times(1)).registerNodeProcessor(eq("sc1"), any(INodeProcessor.class));
        verify(conf, times(1)).registerNodeProcessor(eq("sc2"), any(INodeProcessor.class));
    }


    @Test(expected = JPAUnitFileSyntaxException.class)
    public void testUnknownNode() throws JPAUnitFileReadException, IOException {
        byte[] value = "<jpaunit><someInvalidAndUndUnknownNode/></jpaunit>".getBytes();
        JPAUnitConfiguration conf = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), conf);
    }

}
