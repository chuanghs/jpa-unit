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
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:27
 */
public class EntityNodeProcessor implements INodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(EntityNodeProcessor.class);

    private String className;

    private Set<Class> simpleTypes = new HashSet<Class>(Arrays.asList(
            Integer.class,
            int.class,
            Double.class,
            double.class,
            Boolean.class,
            boolean.class,
            Long.class,
            long.class,
            Float.class,
            float.class,
            Date.class,
            Timestamp.class,
            String.class));

    private Map<Class, Map<String, PropertyDescriptor>> classDescriptors = new HashMap<Class, Map<String, PropertyDescriptor>>();

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private DateFormat tf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    public EntityNodeProcessor(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public synchronized void process(Node entityElement, JPAUnitConfiguration result, JPAUnitConfigurationReader reader) throws JPAUnitNodeProcessingException {
        Class entityClass = null;
        try {
            entityClass = Class.forName(className);
            Object entity = entityClass.newInstance();
            Map<String, PropertyDescriptor> descriptors = getPropertyDescriptors(entityClass);


            processEntity(entityElement, entity);

            result.addEntity(entity);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    private Map<String, PropertyDescriptor> getPropertyDescriptors(Class entityClass) throws IntrospectionException {


        Map<String, PropertyDescriptor> descriptors = classDescriptors.get(entityClass);

        if (descriptors == null) {
            classDescriptors.put(entityClass, descriptors = new HashMap<String, PropertyDescriptor>());

            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(entityClass).getPropertyDescriptors();
            for (PropertyDescriptor pd : propertyDescriptors) {
                descriptors.put(pd.getName(), pd);
            }
        }

        return descriptors;
    }

    private Object processEntity(Node entityElement, Object entity) throws IntrospectionException, InvocationTargetException, ParseException, IllegalAccessException, JPAUnitNodeProcessingException, InstantiationException {
        Map<String, PropertyDescriptor> descriptors = getPropertyDescriptors(entity.getClass());

        NamedNodeMap attributes = entityElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            set(entity, descriptors.get(attribute.getNodeName()), attribute.getNodeValue());
        }

        NodeList children = entityElement.getChildNodes();
        for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
            Node child = children.item(childIndex);
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;

            String name = child.getNodeName();

            PropertyDescriptor pd = descriptors.get(name);
            if (pd == null)
                continue;

            if (isSimpleType(pd.getPropertyType())) {
                String value = null;
                NodeList childValues = child.getChildNodes();
                if (childValues.getLength() > 1)
                    throw new JPAUnitNodeProcessingException("property is allowed to have only one child: CDATA or  text");

                if (childValues.getLength() > 0) {
                    Node childValue = childValues.item(0);

                    if (childValue != null && (childValue.getNodeType() == Node.CDATA_SECTION_NODE || childValue.getNodeType() == Node.TEXT_NODE)) {
                        value = childValue.getNodeValue();
                    }
                }
                set(entity, pd, value);
            } else {
                Object entity1 = processEntity(child, pd.getPropertyType().newInstance());
                set(entity, pd, entity1);
            }
        }
        return entity;
    }


    private boolean isSimpleType(Class<?> propertyType) {
        return simpleTypes.contains(propertyType);
    }

    private void set(Object entity, PropertyDescriptor pd, Object value) throws InvocationTargetException, IllegalAccessException {
        if (pd == null) {
            log.warn("attribute: " + pd.getName() + " does not have corresponding property in class: " + className);
            return;
        }
        Method setter = pd.getWriteMethod();
        if (setter == null) {
            log.warn("there is no setter for property: " + pd.getName() + " of class: " + className);
            return;
        }

        setter.invoke(entity, value);
    }

    private void set(Object entity, PropertyDescriptor pd, String value) throws IllegalAccessException, InvocationTargetException, ParseException {
        if (pd == null) {
            log.warn("attribute: " + pd.getName() + " does not have corresponding property in class: " + className);
            return;
        }
        Method setter = pd.getWriteMethod();
        if (setter == null) {
            log.warn("there is no setter for property: " + pd.getName() + " of class: " + className);
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
