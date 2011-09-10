package org.ormunit.node;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.command.TestSetCommand;
import org.ormunit.exception.FileReadException;

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

    private TestSet testSet;
    private ORMUnitPropertiesReader ormUnit;
    private ORMProvider ormProvider;

    @Before
    public void setUp() {
        ormUnit = spy(new ORMUnitPropertiesReader(workDir, getClass()));
        ormProvider = mock(ORMProvider.class);
        testSet = spy(new TestSet(ormProvider));

    }

    @Test
    public void testIncludeChangeWorkDir1() throws FileReadException {

        TestSet rootTestSet = new TestSet(ormProvider);
        TestSet includedTestSet = new TestSet(rootTestSet);
        IncludeNodeProcessor includeNodeProcessor = spy(new IncludeNodeProcessor(ormUnit));


        doReturn(rootTestSet).when(ormUnit).read(any(InputStream.class), same(rootTestSet));

        doReturn(includedTestSet).when(ormUnit).read(eq("../someOtherFile.xml"), same(includedTestSet));
        doReturn(includedTestSet).when(includeNodeProcessor).createTestSetIfNotReadBefore(eq("/foo/someOtherFile.xml"), same(rootTestSet));

        includeNodeProcessor.include("../someOtherFile.xml", rootTestSet);

        verify(ormUnit, times(1)).read(eq("../someOtherFile.xml"), same(includedTestSet));


    }


    /**
     * /
     * +-foo.xml
     * +-foo
     *   +-bar
     *   +-bar.xml
     *
     * @throws org.ormunit.exception.FileReadException
     */
    @Test
    public void testIncludeChangeWorkDirRecurrent() throws FileReadException {
        byte[] value = "<ormunit><include src=\"../bar.xml\"/></ormunit>".getBytes();

        doReturn(new ByteArrayInputStream("<ormunit><include src=\"../foo.xml\"/></ormunit>".getBytes()))
                .when(ormUnit).getResourceAsStream("/foo/bar.xml");

        doReturn(new ByteArrayInputStream("<ormunit></ormunit>".getBytes()))
                .when(ormUnit).getResourceAsStream("/foo.xml");

        ormUnit.read(new ByteArrayInputStream(value), testSet);

        verify(ormUnit, times(1)).read(eq("../bar.xml"), any(TestSet.class));
        verify(ormUnit, times(1)).read(eq("../foo.xml"), any(TestSet.class));
        verify(ormUnit, times(1)).getResourceAsStream(eq("/foo/bar.xml"));
        verify(ormUnit, times(1)).getResourceAsStream(eq("/foo.xml"));

    }

    @Test
    public void testInclude() throws FileReadException {
        IncludeNodeProcessor includeNodeProcessor = spy(new IncludeNodeProcessor(ormUnit));

        doReturn(new ByteArrayInputStream("<ormunit></ormunit>".getBytes()))
                .when(ormUnit).getResourceAsStream(eq(workDir + "/someOtherFile.xml"));

        includeNodeProcessor.include("someOtherFile.xml", testSet);


        verify(includeNodeProcessor).createTestSetIfNotReadBefore(eq(workDir + "/someOtherFile.xml"), same(testSet));
        verify(ormUnit, times(1)).read(eq("someOtherFile.xml"), any(TestSet.class));

        verify(testSet).addCommand(any(TestSetCommand.class));
    }


}
