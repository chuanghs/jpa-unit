package org.ormunit.node;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.entity.PropertyAccessor;
import org.ormunit.entity.SimplePOJO;
import org.ormunit.entity.SimplePOJO2;
import org.ormunit.exception.ORMUnitFileReadException;

import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 17:56
 */
@RunWith(MockitoJUnitRunner.class)
public class CoreEntityNodeProcessorTest {

    @Mock
    ORMProvider ormProvider;

    ORMUnitPropertiesReader ormUnit;

    @Before
    public void setUp() throws IntrospectionException {
        when(ormProvider.getAccessor(eq(SimplePOJO.class), (Class<?>) any())).thenReturn(new PropertyAccessor(SimplePOJO.class));
        when(ormProvider.getAccessor(eq(SimplePOJO2.class), (Class<?>) any())).thenReturn(new PropertyAccessor(SimplePOJO2.class));
        ormUnit = new ORMUnitPropertiesReader(getClass());
    }

    @Test
    public void testReferencePattern() {
        Assert.assertFalse("ref()".matches(EntityNodeProcessor.ReferencePattern));

        Assert.assertTrue("ref( )".matches(EntityNodeProcessor.ReferencePattern));
        Assert.assertTrue("ref(1)".matches(EntityNodeProcessor.ReferencePattern));
        Assert.assertTrue("ref(someStringReference)".matches(EntityNodeProcessor.ReferencePattern));
    }

    @Test
    public void testSimplePropertiesAttributes() throws ORMUnitFileReadException, ParseException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo integerValue=\"1\" enumValue=\"Value1\" doubleValue=\"1.23\" booleanValue=\"true\" stringValue=\"string\" timestampValue=\"2010-12-18 18:22:00\" dateValue=\"2010-12-18\" />" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(ormProvider));
        ormUnit.read(bais, result);

        SimplePOJO simplePOJO = new SimplePOJO();
        simplePOJO.setIntegerValue(1);
        simplePOJO.setDoubleValue(1.23);
        simplePOJO.setBooleanValue(true);
        simplePOJO.setStringValue("string");

        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2010-12-18 18:22:00");
        simplePOJO.setTimestampValue(new Timestamp(date.getTime()));
        simplePOJO.setDateValue(new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-18"));

        verify(result, times(1)).addCommand(eq(new EntityCommand(simplePOJO, ormProvider.getAccessor(simplePOJO.getClass(), null))));
        verify(result, times(2)).getProvider();

        //verifyNoMoreInteractions(result);
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

        TestSet result = spy(new TestSet(ormProvider));
        ormUnit.read(bais, result);

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

        verify(result, times(1)).addCommand(eq(new EntityCommand(simplePOJO, ormProvider.getAccessor(simplePOJO.getClass(), null))));
        verify(result, times(2)).getProvider();
        //verifyNoMoreInteractions(result);
    }

    @Test
    public void testComplexType() throws ORMUnitFileReadException, ParseException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO2\" /> " +
                "   <pojo integerValue=\"2\"> " +
                "       <doubleValue>1.23</doubleValue> " +
                "       <complexType > " +
                "           <SimplePOJO2> " +
                "               <intValue>2</intValue> " +
                "               <stringValue>1</stringValue> " +
                "           </SimplePOJO2> " +
                "       </complexType>" +
                "   </pojo>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(ormProvider));
        ormUnit.read(bais, result);

        SimplePOJO simplePOJO = new SimplePOJO();
        simplePOJO.setIntegerValue(2);
        simplePOJO.setDoubleValue(1.23);

        SimplePOJO2 complexType = new SimplePOJO2();
        complexType.setIntValue(1);
        complexType.setStringValue("1");
        simplePOJO.setComplexType(complexType);


        verify(result, times(1)).addCommand(eq(new EntityCommand(simplePOJO, ormProvider.getAccessor(simplePOJO.getClass(), null))));
        //verify(result, times(4)).getProvider();

        //verifyNoMoreInteractions(result);
    }

    @Test
    public void testComplexTypeWithReference() throws ORMUnitFileReadException, IntrospectionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo complexType=\"ref(1)\"> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(ormProvider));

        when(ormProvider.getIdType(SimplePOJO2.class)).thenReturn(int.class);

        ormUnit.read(bais, result);

        SimplePOJO entity = new SimplePOJO();
        Set<EntityReference> references = new HashSet<EntityReference>();
        references.add(new EntityReference("complexType", 1));
        verify(result, times(1)).addCommand(eq(new EntityCommand(null, entity, ormProvider.getAccessor(entity.getClass(), null), references)));


        when(ormProvider.getEntity(eq(SimplePOJO2.class), anyInt())).thenReturn(new SimplePOJO2());
        result.execute();

        verify(ormProvider, times(1)).getEntity(eq(SimplePOJO2.class), anyInt());
    }

    @Test
    public void testComplexTypeWithSimpleReferenceAsTag() throws ORMUnitFileReadException, IntrospectionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo> " +
                "       <complexType> " +
                "           <reference>0</reference> " +
                "       </complexType> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(ormProvider));

        when(ormProvider.getIdType(SimplePOJO2.class)).thenReturn(Integer.class);

        ormUnit.read(bais, result);

        SimplePOJO entity = new SimplePOJO();
        Set<EntityReference> references = new HashSet<EntityReference>();
        SimplePOJO2 simplePOJO2 = new SimplePOJO2("0", 0);
        references.add(new EntityReference("complexType", simplePOJO2.getIntValue()));
        verify(result, times(1)).addCommand(eq(new EntityCommand(null, entity, ormProvider.getAccessor(entity.getClass(), null), references)));


        when(ormProvider.getEntity(eq(SimplePOJO2.class), eq(simplePOJO2.getIntValue()))).thenReturn(simplePOJO2);
        result.execute();

        verify(ormProvider, times(1)).getEntity(eq(SimplePOJO2.class), eq(simplePOJO2.getIntValue()));
    }

    @Test
    public void testComplexTypeWithComplexReferenceAsTag() throws ORMUnitFileReadException, IntrospectionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo> " +
                "       <complexType> " +
                "           <reference stringValue=\"0\" intValue=\"0\" /> " +
                "       </complexType> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(ormProvider));

        when(ormProvider.getIdType(SimplePOJO2.class)).thenReturn(SimplePOJO2.class);

        ormUnit.read(bais, result);

        SimplePOJO entity = new SimplePOJO();
        Set<EntityReference> references = new HashSet<EntityReference>();
        SimplePOJO2 id = new SimplePOJO2("0", 0);
        references.add(new EntityReference("complexType", id));
        verify(result, times(1)).addCommand(eq(new EntityCommand(null, entity, ormProvider.getAccessor(entity.getClass(), null), references)));


        when(ormProvider.getEntity(eq(SimplePOJO2.class), eq(id))).thenReturn(id);
        result.execute();

        verify(ormProvider, times(1)).getEntity(eq(SimplePOJO2.class), eq(id));
    }

    @Test
    public void testComplexTypeWithORMReference() throws ORMUnitFileReadException, IntrospectionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO2\" alias=\"pojo2\" />" +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <pojo2 ormId=\"some weird id\" /> " +
                "   <pojo complexType=\"ormref( some weird id )\"> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(ormProvider));


        when(ormProvider.getIdType(SimplePOJO2.class)).thenReturn(int.class);

        ormUnit.read(bais, result);

        EntityCommand entityCommand = new EntityCommand("some weird id", new SimplePOJO2(), ormProvider.getAccessor(SimplePOJO2.class, null), new HashSet<EntityReference>());
        verify(result).addCommand(Mockito.eq(entityCommand));


        HashSet<EntityReference> entityReferences = new HashSet<EntityReference>();
        entityReferences.add(new EntityReference("complexType", "some weird id", EntityReference.Type.ORMUNIT));
        EntityCommand entityCommand1 = new EntityCommand(null, new SimplePOJO(), ormProvider.getAccessor(SimplePOJO.class, null), entityReferences);
        verify(result).addCommand(Mockito.eq(entityCommand1));

        result.execute();
        verify(ormProvider, times(0)).getEntity(eq(SimplePOJO2.class), anyInt());

    }

    @Test
    public void testCollection() throws ORMUnitFileReadException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO2\" alias=\"pojo2\" /> " +
                "   <pojo> " +
                "       <collection>" +
                "           <pojo2 stringValue=\"some string 1\" intValue=\"1\" />" +
                "           <pojo2 stringValue=\"some string 2\" intValue=\"2\" />" +
                "       </collection> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(ormProvider));
        ormUnit.read(bais, result);

        verify(result, times(2)).getNodeProcessor("pojo2");

        SimplePOJO pojo = new SimplePOJO();
        LinkedList<SimplePOJO2> collection = new LinkedList<SimplePOJO2>();
        SimplePOJO2 pojo2 = new SimplePOJO2();
        pojo2.setStringValue("some string 1");
        pojo2.setIntValue(1);
        collection.add(pojo2);

        pojo2 = new SimplePOJO2();
        pojo2.setStringValue("some string 2");
        pojo2.setIntValue(2);
        collection.add(pojo2);
        pojo.setCollection(collection);

        verify(result, times(1)).addCommand(new EntityCommand(pojo, ormProvider.getAccessor(pojo.getClass(), null)));

    }

    @Test
    public void testMap() throws ORMUnitFileReadException {
        ByteArrayInputStream bais = new ByteArrayInputStream(("<ormunit> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO\" alias=\"pojo\" /> " +
                "   <import class=\"org.ormunit.entity.SimplePOJO2\" alias=\"pojo2\" /> " +
                "   <pojo> " +
                "       <map>" +
                "           <entry key=\"1\"><pojo2 stringValue=\"some string 1\" intValue=\"1\" /></entry>" +
                "           <entry key=\"2\"><pojo2 stringValue=\"some string 2\" intValue=\"2\" /></entry>" +
                "       </map> " +
                "   </pojo>" +
                "</ormunit>").getBytes());

        TestSet result = spy(new TestSet(ormProvider));
        ormUnit.read(bais, result);

        verify(result, times(2)).getNodeProcessor("import");
        verify(result, times(2)).getNodeProcessor("pojo2");
    }

}
