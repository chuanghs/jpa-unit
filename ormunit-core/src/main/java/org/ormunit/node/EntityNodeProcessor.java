package org.ormunit.node;

import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitConfiguration;
import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitHelper;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.entity.EntityAccessor;
import org.ormunit.exception.ConvertionException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitFileSyntaxException;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
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
    private final ORMUnit reader;

    public EntityNodeProcessor(String className, ORMUnit reader) {
        this.className = className;
        this.reader = reader;
    }

    public Class getEntityClass() throws ClassNotFoundException {
        return Class.forName(className);
    }

    public synchronized void process(Node entityElement, ORMUnitConfiguration result, ORMUnit reader) throws ORMUnitNodeProcessingException {
        try {
            Object entity = getEntityClass().newInstance();
            Set<EntityReference> references = new HashSet<EntityReference>();
            processEntity(result.getProvider(), entityElement, entity, references);

            result.addCommand(new EntityCommand(entity, references));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    public Object processEntity(ORMProvider provider, Node entityElement, Object entity, Set<EntityReference> references) throws ORMUnitFileReadException {

        EntityAccessor introspector = provider.getAccessor(entity.getClass());

        NamedNodeMap attributes = entityElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            set(provider, entity, attribute.getNodeName(), attribute.getNodeValue(), references);
        }

        NodeList children = entityElement.getChildNodes();
        for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
            Node propertyNode = children.item(childIndex);
            if (propertyNode.getNodeType() != Node.ELEMENT_NODE)
                continue;
            String propertyName = propertyNode.getNodeName();

            if (introspector.isSimpleType(propertyName)) {
                String propertyValue = processSimpleType(provider, propertyNode, introspector, references);
                set(provider, entity, propertyName, propertyValue, references);
            }
            Class type = introspector.getType(propertyName);
            if (type == null)
                throw new ORMUnitNodeProcessingException("no such property: " + propertyName + " in class: " + className);
            if (Collection.class.isAssignableFrom(type)) {
                Collection c = processCollection(provider, propertyNode, introspector, references);
                if (introspector.get(entity, propertyName) != null) {
                    ((Collection) introspector.get(entity, propertyName)).addAll(c);
                } else {
                    introspector.set(entity, propertyName, c);
                }
            } else if (Map.class.isAssignableFrom(type)) {
                Map map = processMap(provider, propertyNode, introspector, references);
                if (introspector.get(entity, propertyName) != null) {
                    ((Map) introspector.get(entity, propertyName)).putAll(map);
                } else {
                    introspector.set(entity, propertyName, map);
                }
            } else {

                if (propertyNode.getChildNodes().getLength() == 1) {
                    Node propertyValueNode = propertyNode.getChildNodes().item(0);
                    String propertyValue;
                    if (propertyValueNode != null && (propertyValueNode.getNodeType() == Node.CDATA_SECTION_NODE || propertyValueNode.getNodeType() == Node.TEXT_NODE)) {
                        propertyValue = propertyValueNode.getNodeValue().trim();
                        set(provider, entity, propertyName, propertyValue, references);
                        continue;
                    }
                }
                Object entity1 = processEntity(provider, propertyNode, introspector.newInstance(propertyName), references);
                introspector.set(entity, propertyName, entity1);
            }
        }
        return entity;
    }


    private String processSimpleType(ORMProvider provider, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws ORMUnitFileSyntaxException {
        String propertyName = propertyNode.getNodeName();
        String propertyValue = null;
        NodeList childValues = propertyNode.getChildNodes();
        if (childValues.getLength() > 1)
            throw new ORMUnitFileSyntaxException("property is allowed to have only one propertyNode: CDATA or  text");

        if (childValues.getLength() > 0) {
            Node propertyValueNode = childValues.item(0);

            if (propertyValueNode != null && (propertyValueNode.getNodeType() == Node.CDATA_SECTION_NODE || propertyValueNode.getNodeType() == Node.TEXT_NODE)) {
                propertyValue = propertyValueNode.getNodeValue().trim();
            }
        }
        return propertyValue;
    }

    private Map processMap(ORMProvider provider, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws ORMUnitFileReadException {
        String propertyName = propertyNode.getNodeName();
        Map map = (Map) introspector.newInstance(propertyName);
        Class[] mapParameterTypes = introspector.getMapParameterTypes(propertyName); // {key, value}

        NodeList entryNodes = propertyNode.getChildNodes();
        for (int i = 0; i < entryNodes.getLength(); i++) {
            Node entryNode = entryNodes.item(i);
            if (entryNode.getNodeType() == Node.ELEMENT_NODE) {
                String key = entryNode.getAttributes().getNamedItem("key").getNodeValue();
                Object value = null;
                NodeList entryValues = entryNode.getChildNodes();
                for (int entry = 0; entry < entryValues.getLength(); entry++) {
                    Node valueNode = entryValues.item(entry);
                    if (valueNode.getNodeType() == Node.ELEMENT_NODE) {


                        Object element = processEntity(provider, valueNode, references);

                        if (!mapParameterTypes[1].isAssignableFrom(element.getClass())) {
                            throw new ORMUnitNodeProcessingException(valueNode.getNodeName() + "(" + element.getClass().getCanonicalName() + ") is not subclass of collection parameter typ: " + mapParameterTypes[1].getCanonicalName());
                        }

                        try {
                            map.put(ORMUnitHelper.convert(mapParameterTypes[0], key),
                                    element);
                        } catch (ConvertionException e) {
                            throw new ORMUnitFileReadException("cannot convert: "+key+" to desired type: "+mapParameterTypes[0].getCanonicalName(), e);
                        }
                    }
                }
            }
        }

        return map;
    }

    private Collection processCollection(ORMProvider provider, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws ORMUnitFileReadException {
        String propertyName = propertyNode.getNodeName();

        Collection c = (Collection) introspector.newInstance(propertyName);
        Class collectionParameterType = introspector.getCollectionParameterType(propertyName);

        NodeList collectionNodes = propertyNode.getChildNodes();
        for (int i = 0; i < collectionNodes.getLength(); i++) {
            Node elementNode = collectionNodes.item(i);
            if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
                Object element = processEntity(provider, elementNode, references);

                if (!collectionParameterType.isAssignableFrom(element.getClass())) {
                    throw new ORMUnitNodeProcessingException(elementNode.getNodeName() + "(" + element.getClass().getCanonicalName() + ") is not subclass of collection parameter typ: " + collectionParameterType.getCanonicalName());
                }

                c.add(element);
            }
        }
        return c;

    }

    private Object processEntity(ORMProvider provider, Node valueNode, Set<EntityReference> references) throws ORMUnitFileReadException {
        INodeProcessor nodeProcessor = reader.getNodeProcessor(valueNode.getNodeName());

        Object element = null;
        if (nodeProcessor instanceof EntityNodeProcessor) {
            Class entityClass = null;
            try {
                entityClass = ((EntityNodeProcessor) nodeProcessor).getEntityClass();
            } catch (ClassNotFoundException e) {
                throw new ORMUnitFileReadException(e);
            }


            try {
                element = processEntity(
                        provider,
                        valueNode,
                        entityClass.newInstance(),
                        references);
            } catch (InstantiationException e) {
                throw new ORMUnitFileReadException(e);
            } catch (IllegalAccessException e) {
                throw new ORMUnitFileReadException(e);
            }
        } else
            throw new ORMUnitFileSyntaxException("");

        return element;
    }


    private void set(ORMProvider provider, Object entity, String propertyName, String value, Set<EntityReference> references) {
        EntityAccessor introspector = provider.getAccessor(entity.getClass());
        try {
            if (value != null && value.matches(ReferencePattern)) {
                value = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")"));
                references.add(new EntityReference(
                        introspector,
                        propertyName,
                        ORMUnitHelper.convert(
                                provider.getIdType(introspector.getType(propertyName)),
                                value)));

            } else {
                introspector.set(entity, propertyName, ORMUnitHelper.convert(
                        introspector.getType(propertyName),
                        value));
            }
        } catch (ConvertionException e) {
            throw new ORMUnitNodeProcessingException(e);
        }
    }

}
