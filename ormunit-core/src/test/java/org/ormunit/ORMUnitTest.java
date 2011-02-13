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
import org.ormunit.node.ANodeProcessor;
import org.ormunit.node.ImportNodeProcessor;
import org.ormunit.node.IncludeNodeProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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

        verify(testSet, times(1)).registerNodeProcessor(eq("sc1"), any(ANodeProcessor.class));
        verify(testSet, times(1)).registerNodeProcessor(eq("sc2"), any(ANodeProcessor.class));
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
    public void testInclude() throws ORMUnitFileReadException {
        String workDir = "/" + ORMUnit.class.getPackage().getName().replace(".", "/");

        IncludeNodeProcessor includeNodeProcessor = new IncludeNodeProcessor(ormUnit);

        doReturn(new ByteArrayInputStream("<ormunit></ormunit>".getBytes())).when(ormUnit).getResourceAsStream(eq(workDir + "/someOtherFile.xml"));

        includeNodeProcessor.include("someOtherFile.xml", testSet);


        verify(ormUnit, times(1)).read(eq("someOtherFile.xml"), same(testSet));
    }

    @Test
    public void testIncludeChangeWorkDir1() throws ORMUnitFileReadException {
        //byte[] value = "<ormunit><include src=\"../someOtherFile.xml\"/></ormunit>".getBytes();

        String workDir = "/" + ORMUnit.class.getPackage().getName().replace(".", "/");

        IncludeNodeProcessor includeNodeProcessor = new IncludeNodeProcessor(ormUnit);

        doReturn(testSet).when(ormUnit).read(any(InputStream.class), same(testSet));

        includeNodeProcessor.include("../someOtherFile.xml", testSet);

        verify(ormUnit, times(1)).read(eq("../someOtherFile.xml"), same(testSet));
        verify(ormUnit, times(1)).getResourceAsStream(eq(workDir + "/../someOtherFile.xml"));

        assertEquals(workDir, ormUnit.getCurrentDir());
    }


    @Test
    public void testIncludeChangeWorkDirRecurrent() throws ORMUnitFileReadException {
        byte[] value = "<ormunit><include src=\"../someOtherFile.xml\"/></ormunit>".getBytes();

        String workDir = "/" + ORMUnit.class.getPackage().getName().replace(".", "/");


//        doReturn(testSet).when(ormUnit).read(any(InputStream.class), same(testSet));
        doReturn(new ByteArrayInputStream("<ormunit><include src=\"../someOtherFile.xml\"/></ormunit>".getBytes())).when(ormUnit).getResourceAsStream(workDir + "/../someOtherFile.xml");
        doReturn(new ByteArrayInputStream("<ormunit></ormunit>".getBytes())).when(ormUnit).getResourceAsStream(workDir + "/../../someOtherFile.xml");

        ormUnit.read(new ByteArrayInputStream(value), testSet);

        verify(ormUnit, times(2)).read(eq("../someOtherFile.xml"), same(testSet));
        verify(ormUnit, times(1)).getResourceAsStream(eq(workDir + "/../someOtherFile.xml"));
        verify(ormUnit, times(1)).getResourceAsStream(eq(workDir + "/../../someOtherFile.xml"));

        assertEquals(workDir, ormUnit.getCurrentDir());
    }
}
