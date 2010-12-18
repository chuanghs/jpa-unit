package org.jpaunit.node;

import org.jpaunit.JPAUnitConfiguration;
import org.jpaunit.JPAUnitConfigurationReader;
import org.jpaunit.exception.JPAUnitNodeProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:27
 */
public class EntityNodeProcessor implements INodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(EntityNodeProcessor.class);

    private String className;

    public EntityNodeProcessor(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }


    private Map<String, PropertyDescriptor> descriptors = null;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private DateFormat tf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    public synchronized void process(Node jpaUnitElement, JPAUnitConfiguration result, JPAUnitConfigurationReader reader) throws JPAUnitNodeProcessingException {
        Class entityClass = null;
        try {
            entityClass = Class.forName(className);
            Object entity = entityClass.newInstance();
            if (descriptors == null) {

                descriptors = new HashMap<String, PropertyDescriptor>();

                PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(entityClass).getPropertyDescriptors();
                for (PropertyDescriptor pd : propertyDescriptors) {
                    descriptors.put(pd.getName(), pd);
                }
            }

            NamedNodeMap attributes = jpaUnitElement.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                set(entity, attribute.getNodeName(), attribute.getNodeValue());
            }

            NodeList children = jpaUnitElement.getChildNodes();
            for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
                Node child = children.item(childIndex);
                if (child.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                String name = child.getNodeName();
                String value = null;
                NodeList childValues = child.getChildNodes();
                if (childValues.getLength() != 1)
                    throw new JPAUnitNodeProcessingException("property element is allowed to have only one child: CDATA or  text");

                Node childValue = childValues.item(0);

                if (childValue != null && (childValue.getNodeType() == Node.CDATA_SECTION_NODE || childValue.getNodeType() == Node.TEXT_NODE)) {
                    value = childValue.getNodeValue();
                }


                set(entity, name, value);
            }

            result.addEntity(entity);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private void set(Object entity, String name, String value) throws IllegalAccessException, InvocationTargetException, ParseException {
        PropertyDescriptor pd = descriptors.get(name);
        if (pd == null) {
            log.warn("attribute: " + name + " does not have corresponding property in class: " + className);
            return;
        }
        Method setter = pd.getWriteMethod();
        if (setter == null) {
            log.warn("there is no setter for property: " + name + " of class: " + className);
            return;
        }


        Class<?> propertyType = pd.getPropertyType();
        if (propertyType.equals(Integer.class) || propertyType.equals(int.class)) {
            setter.invoke(entity, Integer.parseInt(value));
        } else if (propertyType.equals(Double.class) || propertyType.equals(double.class)) {
            setter.invoke(entity, Double.parseDouble(value));
        } else if (propertyType.equals(Boolean.class) || propertyType.equals(boolean.class)) {
            setter.invoke(entity, Boolean.parseBoolean(value));
        } else if (propertyType.equals(Long.class) || propertyType.equals(long.class)) {
            setter.invoke(entity, Long.parseLong(value));
        } else if (propertyType.equals(Float.class) || propertyType.equals(float.class)) {
            setter.invoke(entity, Float.parseFloat(value));
        } else if (propertyType.equals(Date.class)) {
            setter.invoke(entity, df.parse(value));
        } else if (propertyType.equals(Timestamp.class)) {
            setter.invoke(entity, new Timestamp(tf.parse(value).getTime()));
        } else if (propertyType.equals(String.class)) {
            setter.invoke(entity, value);
        }
    }
}
