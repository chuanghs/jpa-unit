package org.ormunit.node;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitConfiguration;
import org.ormunit.ORMUnitConfigurationReader;
import org.ormunit.command.EntityCommand;
import org.ormunit.entity.SimplePOJO;
import org.ormunit.entity.SimplePOJO2;
import org.ormunit.exception.ORMUnitFileReadException;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 17:56
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityNodeProcessorTest {


    @Test
    public void testSimplePropertiesAttributes() throws ORMUnitFileReadException, ParseException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo integerValue=\"1\" doubleValue=\"1.23\" booleanValue=\"true\" stringValue=\"string\" timestampValue=\"2010-12-18 18:22:00\" dateValue=\"2010-12-18\" />" +
                "</ormunit>").getBytes());

        ORMUnitConfiguration result = spy(new ORMUnitConfiguration(mock(ORMProvider.class)));
        new ORMUnitConfigurationReader().read(bais, result);

        SimplePOJO simplePOJO = new SimplePOJO();
        simplePOJO.setIntegerValue(1);
        simplePOJO.setDoubleValue(1.23);
        simplePOJO.setBooleanValue(true);
        simplePOJO.setStringValue("string");

        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2010-12-18 18:22:00");
        simplePOJO.setTimestampValue(new Timestamp(date.getTime()));
        simplePOJO.setDateValue(new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-18"));
        verify(result, times(1)).addImport(anyString(), anyString());
        verify(result, times(1)).addCommand(eq(new EntityCommand(simplePOJO)));
        verify(result, times(1)).getProvider();

        verifyNoMoreInteractions(result);
    }

    @Test
    public void testSimplePropertiesElements() throws ORMUnitFileReadException, ParseException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo integerValue=\"2\" longValue=\"23\"> " +
                "       <integerValue>1</integerValue> " +
                "       <doubleValue>1.23</doubleValue> " +
                "       <floatValue>1.23</floatValue> " +
                "       <booleanValue>true</booleanValue> " +
                "       <stringValue>string</stringValue> " +
                "       <timestampValue>2010-12-18 18:22:00</timestampValue> " +
                "       <dateValue>2010-12-18</dateValue>" +
                "   </pojo>" +
                "</ormunit>").getBytes());

        ORMUnitConfiguration result = spy(new ORMUnitConfiguration(mock(ORMProvider.class)));
        new ORMUnitConfigurationReader().read(bais, result);

        SimplePOJO simplePOJO = new SimplePOJO();
        simplePOJO.setIntegerValue(1);
        simplePOJO.setDoubleValue(1.23);
        simplePOJO.setFloatValue(1.23f);
        simplePOJO.setLongValue(23l);
        simplePOJO.setBooleanValue(true);
        simplePOJO.setStringValue("string");

        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2010-12-18 18:22:00");
        simplePOJO.setTimestampValue(new Timestamp(date.getTime()));
        simplePOJO.setDateValue(new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-18"));
        verify(result, times(1)).addImport(anyString(), anyString());
        verify(result, times(1)).addCommand(eq(new EntityCommand(simplePOJO)));
        verify(result, times(1)).getProvider();
        verifyNoMoreInteractions(result);
    }


    @Test
    public void testComplexType() throws ORMUnitFileReadException, ParseException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo integerValue=\"2\"> " +
                "       <booleanValue></booleanValue> " +
                "       <doubleValue>1.23</doubleValue> " +
                "       <complexType intValue=\"1\"> " +
                "           <stringValue>1</stringValue> " +
                "       </complexType>" +
                "   </pojo>" +
                "</ormunit>").getBytes());

        ORMUnitConfiguration result = spy(new ORMUnitConfiguration(mock(ORMProvider.class)));
        new ORMUnitConfigurationReader().read(bais, result);

        SimplePOJO simplePOJO = new SimplePOJO();
        simplePOJO.setIntegerValue(2);
        simplePOJO.setDoubleValue(1.23);

        SimplePOJO2 complexType = new SimplePOJO2();
        complexType.setIntValue(1);
        complexType.setStringValue("1");
        simplePOJO.setComplexType(complexType);

        verify(result, times(1)).addImport(anyString(), anyString());
        verify(result, times(1)).addCommand(eq(new EntityCommand(simplePOJO)));
        verify(result, times(1)).getProvider();

        verifyNoMoreInteractions(result);
    }

}
