package org.jpaunit.node;

import org.jpaunit.JPAUnitConfiguration;
import org.jpaunit.JPAUnitConfigurationReader;
import org.jpaunit.JPAUnitHelper;
import org.jpaunit.command.EntityCommand;
import org.jpaunit.command.EntityReference;
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
import java.text.ParseException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:27
 */
public class EntityNodeProcessor implements INodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(EntityNodeProcessor.class);
    private static Set<Class> simpleTypes = new HashSet<Class>(Arrays.asList(
            Integer.class, int.class,
            Double.class, double.class,
            Boolean.class, boolean.class,
            Long.class, long.class,
            Float.class, float.class,
            Date.class,
            Timestamp.class,
            String.class));

    public static final String ReferencePattern = "ref\\(.+\\)";


    private Map<Class, Map<String, PropertyDescriptor>> classDescriptors = new HashMap<Class, Map<String, PropertyDescriptor>>();

    private String className;

    public EntityNodeProcessor(String className) {
        this.className = className;
    }

    public synchronized void process(Node entityElement, JPAUnitConfiguration result, JPAUnitConfigurationReader reader) throws JPAUnitNodeProcessingException {
        Class entityClass = null;
        try {
            entityClass = Class.forName(className);
            Object entity = entityClass.newInstance();
            Map<String, PropertyDescriptor> descriptors = getPropertyDescriptors(entityClass);


            Set<EntityReference> references = new HashSet<EntityReference>();
            processEntity(entityElement, entity, references);

            result.addCommand(new EntityCommand(entity, references));
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    private Object processEntity(Node entityElement, Object entity, Set<EntityReference> references) throws IntrospectionException, InvocationTargetException, ParseException, IllegalAccessException, JPAUnitNodeProcessingException, InstantiationException {
        Map<String, PropertyDescriptor> descriptors = getPropertyDescriptors(entity.getClass());

        NamedNodeMap attributes = entityElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            set(entity, descriptors.get(attribute.getNodeName()), attribute.getNodeValue(), references);
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
                String propertyValue = null;
                NodeList childValues = child.getChildNodes();
                if (childValues.getLength() > 1)
                    throw new JPAUnitNodeProcessingException("property is allowed to have only one child: CDATA or  text");

                if (childValues.getLength() > 0) {
                    Node propertyValueNode = childValues.item(0);

                    if (propertyValueNode != null && (propertyValueNode.getNodeType() == Node.CDATA_SECTION_NODE || propertyValueNode.getNodeType() == Node.TEXT_NODE)) {
                        propertyValue = propertyValueNode.getNodeValue();
                    }
                }
                set(entity, pd, propertyValue, references);
            } else {

                if (child.getChildNodes().getLength() == 1){
                    Node propertyValueNode = child.getChildNodes().item(0);
                    String propertyValue;
                    if (propertyValueNode != null && (propertyValueNode.getNodeType() == Node.CDATA_SECTION_NODE || propertyValueNode.getNodeType() == Node.TEXT_NODE)) {
                        propertyValue = propertyValueNode.getNodeValue().trim();
                        set(entity, pd, propertyValue, references);
                        continue;
                    }
                }
                Object entity1 = processEntity(child, pd.getPropertyType().newInstance(), references);
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

    private void set(Object entity, PropertyDescriptor pd, String value, Set<EntityReference> references) throws IllegalAccessException, InvocationTargetException, ParseException {

        if (value != null && value.matches(ReferencePattern)) {
            value = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")"));
            references.add(new EntityReference(entity, pd, JPAUnitHelper.convert(JPAUnitHelper.getIdType(pd.getPropertyType()), value)));
            return;
        }

        if (pd == null) {
            log.warn("attribute: " + pd.getName() + " does not have corresponding property in class: " + className);
            return;
        }
        Method setter = pd.getWriteMethod();
        if (setter == null) {
            log.warn("there is no setter for property: " + pd.getName() + " of class: " + className);
            return;
        }

        setter.invoke(entity, JPAUnitHelper.convert(pd.getPropertyType(), value));

    }

}
