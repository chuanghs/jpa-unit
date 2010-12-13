package org.jpaunit;

import org.jpaunit.exception.JPAUnitConfigurationException;
import org.jpaunit.exception.JPAUnitFileReadException;
import org.jpaunit.exception.JPAUnitFileSyntaxException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
        JPAUnitConfiguration mock = mock(JPAUnitConfiguration.class);
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), mock);

        verify(mock, times(1)).addStatement(Matchers.eq("code1"));
        verify(mock, times(1)).addStatement(Matchers.eq("code2"));
        verify(mock, times(1)).addStatement(Matchers.eq("code3"));

        verifyNoMoreInteractions(mock);

    }

    @Test(expected = JPAUnitFileSyntaxException.class)
    public void testStatementWith2Children() throws JPAUnitFileReadException, IOException {

        byte[] value = "<jpaunit><statement code=\"this code shouldnt be added\"><![CDATA[code1]]><somesubelem /></statement></jpaunit>".getBytes();
        JPAUnitConfiguration mock = mock(JPAUnitConfiguration.class);
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), mock);

        verifyNoMoreInteractions(mock);

    }


    @Test
    public void testImports() throws JPAUnitFileReadException, IOException {
        byte[] value = "<jpaunit><import class=\"com.example.SomeClass1\" alias=\"sc1\"/><import class=\"com.example.SomeClass\"/></jpaunit>".getBytes();
        
        JPAUnitConfiguration mock = mock(JPAUnitConfiguration.class);
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), mock);

        verify(mock).addImport(eq("com.example.SomeClass1"),  eq("sc1"));
        verify(mock).addImport(eq("com.example.SomeClass"),  eq("SomeClass"));
        
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
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), new JPAUnitConfiguration());
    }

}
