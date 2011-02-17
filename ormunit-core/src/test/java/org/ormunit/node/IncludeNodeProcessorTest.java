package org.ormunit.node;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.ORMProvider;
import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.command.TestSetCommand;
import org.ormunit.exception.ORMUnitFileReadException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 21.12.10
 * Time: 23:59
 */
@RunWith(JUnit4.class)
public class IncludeNodeProcessorTest {


    private static final String workDir = "/foo/bar";

    private ORMUnitTestSet testSet;
    private ORMUnit ormUnit;
    private ORMProvider ormProvider;

    @Before
    public void setUp() {
        ormUnit = spy(new ORMUnit(workDir, getClass()));
        ormProvider = mock(ORMProvider.class);
        testSet = spy(new ORMUnitTestSet(ormProvider));

    }

    @Test
    public void testIncludeChangeWorkDir1() throws ORMUnitFileReadException {
        //byte[] value = "<ormunit><include src=\"../someOtherFile.xml\"/></ormunit>".getBytes();



        ORMUnitTestSet rootTestSet = new ORMUnitTestSet(ormProvider);
        ORMUnitTestSet includedTestSet = new ORMUnitTestSet(rootTestSet);
        IncludeNodeProcessor includeNodeProcessor = spy(new IncludeNodeProcessor(ormUnit));


        doReturn(rootTestSet).when(ormUnit).read(any(InputStream.class), same(rootTestSet));

        doReturn(includedTestSet).when(ormUnit).read(eq("../someOtherFile.xml"), same(includedTestSet));
        doReturn(includedTestSet).when(includeNodeProcessor).createTestSetIfNotReadBefore(eq("/foo/someOtherFile.xml"), same(rootTestSet));

        includeNodeProcessor.include("../someOtherFile.xml", rootTestSet);

        verify(ormUnit, times(1)).read(eq("../someOtherFile.xml"), same(includedTestSet));


        assertEquals(workDir, ormUnit.getCurrentDir());
    }


    /**
     * /
     * +-foo.xml
     * +-foo
     *   +-bar
     *   +-bar.xml
     *
     * @throws ORMUnitFileReadException
     */
    @Test
    public void testIncludeChangeWorkDirRecurrent() throws ORMUnitFileReadException {
        byte[] value = "<ormunit><include src=\"../bar.xml\"/></ormunit>".getBytes();

        String workDir = "/foo/bar";


        doReturn(new ByteArrayInputStream("<ormunit><include src=\"../foo.xml\"/></ormunit>".getBytes()))
                .when(ormUnit).getResourceAsStream("/foo/bar.xml");

        doReturn(new ByteArrayInputStream("<ormunit></ormunit>".getBytes()))
                .when(ormUnit).getResourceAsStream("/foo.xml");

        ormUnit.read(new ByteArrayInputStream(value), testSet);

        verify(ormUnit, times(1)).read(eq("../bar.xml"), any(ORMUnitTestSet.class));
        verify(ormUnit, times(1)).read(eq("../foo.xml"), any(ORMUnitTestSet.class));
        verify(ormUnit, times(1)).getResourceAsStream(eq("/foo/bar.xml"));
        verify(ormUnit, times(1)).getResourceAsStream(eq("/foo.xml"));

        assertEquals(workDir, ormUnit.getCurrentDir());
    }

    @Test
    public void testInclude() throws ORMUnitFileReadException {
        IncludeNodeProcessor includeNodeProcessor = spy(new IncludeNodeProcessor(ormUnit));

        doReturn(new ByteArrayInputStream("<ormunit></ormunit>".getBytes()))
                .when(ormUnit).getResourceAsStream(eq(workDir + "/someOtherFile.xml"));

        includeNodeProcessor.include("someOtherFile.xml", testSet);


        verify(includeNodeProcessor).createTestSetIfNotReadBefore(eq(workDir + "/someOtherFile.xml"), same(testSet));
        verify(ormUnit, times(1)).read(eq("someOtherFile.xml"), any(ORMUnitTestSet.class));

        verify(testSet).addCommand(any(TestSetCommand.class));
    }


}
