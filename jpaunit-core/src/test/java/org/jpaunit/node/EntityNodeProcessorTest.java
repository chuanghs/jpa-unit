package org.jpaunit.node;

import org.jpaunit.JPAUnitConfiguration;
import org.jpaunit.JPAUnitConfigurationReader;
import org.jpaunit.exception.JPAUnitFileReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
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
    public void testSimplePropertiesAttributes() throws JPAUnitFileReadException, ParseException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<jpaunit> " +
                "   <import class=\"org.jpaunit.node.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo integerValue=\"1\" doubleValue=\"1.23\" booleanValue=\"true\" stringValue=\"string\" timestampValue=\"2010-12-18 18:22:00\" dateValue=\"2010-12-18\" />" +
                "</jpaunit>").getBytes());

        JPAUnitConfiguration result = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(bais, result);

        SimplePOJO simplePOJO = new SimplePOJO();
        simplePOJO.setIntegerValue(1);
        simplePOJO.setDoubleValue(1.23);
        simplePOJO.setBooleanValue(true);
        simplePOJO.setStringValue("string");

        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2010-12-18 18:22:00");
        simplePOJO.setTimestampValue(new Timestamp(date.getTime()));
        simplePOJO.setDateValue(new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-18"));
        verify(result, times(1)).addImport(anyString(), anyString());
        verify(result, times(1)).addEntity(eq(simplePOJO));
        verifyNoMoreInteractions(result);
    }

    @Test
    public void testSimplePropertiesElements() throws JPAUnitFileReadException, ParseException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<jpaunit> " +
                "   <import class=\"org.jpaunit.node.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo integerValue=\"2\" longValue=\"23\"> " +
                "       <integerValue>1</integerValue> " +
                "       <doubleValue>1.23</doubleValue> " +
                "       <floatValue>1.23</floatValue> " +
                "       <booleanValue>true</booleanValue> " +
                "       <stringValue>string</stringValue> " +
                "       <timestampValue>2010-12-18 18:22:00</timestampValue> " +
                "       <dateValue>2010-12-18</dateValue>" +
                "   </pojo>"+
                "</jpaunit>").getBytes());

        JPAUnitConfiguration result = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(bais, result);

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
        verify(result, times(1)).addEntity(eq(simplePOJO));
        verifyNoMoreInteractions(result);
    }


    @Test
    public void testComplexType() throws JPAUnitFileReadException, ParseException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<jpaunit> " +
                "   <import class=\"org.jpaunit.node.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo integerValue=\"2\"> " +
                "       <booleanValue></booleanValue> " +
                "       <doubleValue>1.23</doubleValue> " +
                "       <complexType intValue=\"1\"> " +
                "           <stringValue>1</stringValue> "+
                "       </complexType>"+
                "   </pojo>"+
                "</jpaunit>").getBytes());

        JPAUnitConfiguration result = spy(new JPAUnitConfiguration());
        new JPAUnitConfigurationReader().read(bais, result);

        SimplePOJO simplePOJO = new SimplePOJO();
        simplePOJO.setIntegerValue(2);
        simplePOJO.setDoubleValue(1.23);

        SimplePOJO2 complexType = new SimplePOJO2();
        complexType.setIntValue(1);
        complexType.setStringValue("1");
        simplePOJO.setComplexType(complexType);

        verify(result, times(1)).addImport(anyString(), anyString());
        verify(result, times(1)).addEntity(eq(simplePOJO));
        verifyNoMoreInteractions(result);
    }

}
