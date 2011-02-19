package org.ormunit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.command.StatementCommand;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitFileSyntaxException;
import org.ormunit.node.NodeProcessor;
import org.ormunit.node.ImportNodeProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 15:40
 */
@RunWith(MockitoJUnitRunner.class)
public class ORMUnitTest {


    ORMUnitTestSet testSet;
    private ORMUnit ormUnit;

    @Before
    public void setUp() {
        ormUnit = spy(new ORMUnit(getClass()));
        testSet = spy(new ORMUnitTestSet(mock(ORMProvider.class)));

    }


    @Test
    public void testReadStatements() throws ORMUnitFileReadException, IOException {

        byte[] value = ("<ormunit> " +
                "<statement code=\"this code shouldnt be added\">code0</statement>" +
                "<statement code=\"this code shouldnt be added\"><![CDATA[code1]]></statement>" +
                "<statement code=\"code2\" /><statement code=\"code3\" /></ormunit>").getBytes();


        ormUnit.read(new ByteArrayInputStream(value), testSet);

        verify(testSet, times(4)).getNodeProcessor(eq("statement"));

        verify(testSet, times(1)).addCommand(Matchers.eq(new StatementCommand("code0")));
        verify(testSet, times(1)).addCommand(Matchers.eq(new StatementCommand("code1")));
        verify(testSet, times(1)).addCommand(Matchers.eq(new StatementCommand("code2")));
        verify(testSet, times(1)).addCommand(Matchers.eq(new StatementCommand("code3")));

        //verifyNoMoreInteractions(testSet);

    }

    @Test(expected = ORMUnitFileSyntaxException.class)
    public void testStatementWith2Children() throws ORMUnitFileReadException, IOException {

        byte[] value = "<ormunit> <statement code=\"this code shouldnt be added\"><![CDATA[code1]]><somesubelem /></statement></ormunit>".getBytes();


        ormUnit.read(new ByteArrayInputStream(value), testSet);

        verify(testSet, times(2)).getNodeProcessor(eq("statements"));

        verifyNoMoreInteractions(testSet);

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

        new ORMUnit(getClass())
                .read(new ByteArrayInputStream(value), testSet);

    }

    @Test
    public void testRegisterNodeProcessor() throws ORMUnitFileReadException, IOException {
        byte[] value = "<ormunit><import class=\"$com._example.SomeClass1\" alias=\"sc1\"/><import class=\"com.example.SomeClass\" alias=\"sc2\"/></ormunit>".getBytes();


        ormUnit.read(new ByteArrayInputStream(value), testSet);

        verify(testSet, times(1)).registerNodeProcessor(eq("sc1"), any(NodeProcessor.class));
        verify(testSet, times(1)).registerNodeProcessor(eq("sc2"), any(NodeProcessor.class));
    }


    @Test(expected = ORMUnitFileSyntaxException.class)
    public void testUnknownNode() throws ORMUnitFileReadException, IOException {
        byte[] value = "<ormunit><someInvalidAndUndUnknownNode/></ormunit>".getBytes();

        ormUnit.read(new ByteArrayInputStream(value), testSet);
    }


    @Test(expected = ORMUnitFileReadException.class)
    public void testInvalidSyntax() throws ORMUnitFileReadException {
        byte[] value = "some non xml content".getBytes();
        ormUnit.read(new ByteArrayInputStream(value), testSet);
    }



    @Test
    public void testNormalize(){
        assertArrayEquals(new String[]{"/parent/path/", "file.xml"}, ormUnit.normalizePath("/parent/path", "file.xml"));
        assertArrayEquals(new String[]{"/foo/", "file.xml"}, ormUnit.normalizePath("/parent/path", "/foo/file.xml"));
        assertArrayEquals(new String[]{"/", "file.xml"}, ormUnit.normalizePath("/foo", "../file.xml"));
        assertArrayEquals(new String[]{"/", "file.xml"}, ormUnit.normalizePath("/foo/", "../file.xml"));

        assertArrayEquals(new String[]{"/foo/", "file.xml"}, ormUnit.normalizePath("/foo", "./file.xml"));
    }

}
