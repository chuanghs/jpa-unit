package org.ormunit.node;

import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitConfiguration;
import org.ormunit.ORMUnitConfigurationReader;
import org.ormunit.ORMUnitHelper;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.entity.EntityAccessor;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:27
 */
public class EntityNodeProcessor implements INodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(EntityNodeProcessor.class);
    public static final String ReferencePattern = "ref\\(.+\\)";

    private final String className;
    private final ORMUnitConfigurationReader reader;

    public EntityNodeProcessor(String className, ORMUnitConfigurationReader reader) {
        this.className = className;
        this.reader = reader;
    }

    public synchronized void process(Node entityElement, ORMUnitConfiguration result, ORMUnitConfigurationReader reader) throws ORMUnitNodeProcessingException {
        Class entityClass = null;
        try {
            entityClass = Class.forName(className);
            Object entity = entityClass.newInstance();
            Set<EntityReference> references = new HashSet<EntityReference>();
            processEntity(result.getProvider(), entityElement, entity, references);

            result.addCommand(new EntityCommand(entity, references));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    private Object processEntity(ORMProvider provider, Node entityElement, Object entity, Set<EntityReference> references) throws IntrospectionException, InvocationTargetException, ParseException, IllegalAccessException, ORMUnitNodeProcessingException, InstantiationException {

        EntityAccessor introspector = provider.getAccessor(entity.getClass());

        NamedNodeMap attributes = entityElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            set(provider, entity, attribute.getNodeName(), attribute.getNodeValue(), references);
        }

        NodeList children = entityElement.getChildNodes();
        for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
            Node child = children.item(childIndex);
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            String propertyName = child.getNodeName();

            if (introspector.isSimpleType(propertyName)) {
                String propertyValue = null;
                NodeList childValues = child.getChildNodes();
                if (childValues.getLength() > 1)
                    throw new ORMUnitNodeProcessingException("property is allowed to have only one child: CDATA or  text");

                if (childValues.getLength() > 0) {
                    Node propertyValueNode = childValues.item(0);

                    if (propertyValueNode != null && (propertyValueNode.getNodeType() == Node.CDATA_SECTION_NODE || propertyValueNode.getNodeType() == Node.TEXT_NODE)) {
                        propertyValue = propertyValueNode.getNodeValue();
                    }
                }
                set(provider, entity, propertyName, propertyValue, references);
            } else {

                if (child.getChildNodes().getLength() == 1) {
                    Node propertyValueNode = child.getChildNodes().item(0);
                    String propertyValue;
                    if (propertyValueNode != null && (propertyValueNode.getNodeType() == Node.CDATA_SECTION_NODE || propertyValueNode.getNodeType() == Node.TEXT_NODE)) {
                        propertyValue = propertyValueNode.getNodeValue().trim();
                        set(provider, entity, propertyName, propertyValue, references);
                        continue;
                    }
                }
                Object entity1 = processEntity(provider, child, introspector.newInstance(propertyName), references);
                introspector.set(entity, propertyName, entity1);
            }
        }
        return entity;
    }

    private void set(ORMProvider provider, Object entity, String propertyName, String value, Set<EntityReference> references) throws IllegalAccessException, InvocationTargetException, ParseException, IntrospectionException {
        EntityAccessor introspector = provider.getAccessor(entity.getClass());
        if (value != null && value.matches(ReferencePattern)) {
            value = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")"));
            references.add(new EntityReference(
                    introspector,
                    propertyName,
                    ORMUnitHelper.convert(
                            provider.getIdType(
                                    introspector.getType(propertyName)),
                            value)));
            return;
        }

        introspector.set(entity, propertyName, ORMUnitHelper.convert(introspector.getType(propertyName), value));
    }

}
