package org.jpaunit;

import org.jpaunit.exception.JPAUnitFileReadException;
import org.jpaunit.exception.JPAUnitFileSyntaxException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
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
        JPAUnitConfiguration mock = Mockito.mock(JPAUnitConfiguration.class);
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), mock);

        Mockito.verify(mock, Mockito.times(1)).addStatement(Matchers.eq("code1"));
        Mockito.verify(mock, Mockito.times(1)).addStatement(Matchers.eq("code2"));
        Mockito.verify(mock, Mockito.times(1)).addStatement(Matchers.eq("code3"));

        Mockito.verifyNoMoreInteractions(mock);

    }

    @Test(expected = JPAUnitFileSyntaxException.class)
    public void testStatementWith2Children() throws JPAUnitFileReadException, IOException {

        byte[] value = "<jpaunit><statement code=\"this code shouldnt be added\"><![CDATA[code1]]><somesubelem /></statement></jpaunit>".getBytes();
        JPAUnitConfiguration mock = Mockito.mock(JPAUnitConfiguration.class);
        new JPAUnitConfigurationReader().read(new ByteArrayInputStream(value), mock);

        Mockito.verifyNoMoreInteractions(mock);

    }

}
