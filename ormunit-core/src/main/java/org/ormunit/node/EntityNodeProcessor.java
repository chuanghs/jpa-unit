package org.ormunit.node;

import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitHelper;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.entity.EntityAccessor;
import org.ormunit.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:27
 */
public class EntityNodeProcessor extends NodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(EntityNodeProcessor.class);
    public static final String ReferencePattern = "ref\\(.+\\)";
    public static final String ORMReferencePattern = "ormref\\(.+\\)";

    private final String className;


    public EntityNodeProcessor(String className) {
        super(null);
        this.className = className;
    }

    public Class getEntityClass() throws ClassNotFoundException {
        return Class.forName(className);
    }

    public synchronized void process(Node node, ORMUnitTestSet testSet) throws ORMUnitNodeProcessingException {
        try {
            Object entity = getEntityClass().newInstance();
            Set<EntityReference> references = new HashSet<EntityReference>();

            entity = processEntity(node, entity, references, testSet);
            String ormId = extractOrmId(node);
            EntityAccessor accessor = testSet.getProvider().getAccessor(entity.getClass());

            testSet.addCommand(new EntityCommand(ormId,
                    entity,
                    accessor,
                    references));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    public Object processEntity(Node entityElement, Object entity, Set<EntityReference> references, ORMUnitTestSet testSet) throws ORMUnitFileReadException {
        ORMProvider provider = testSet.getProvider();
        EntityAccessor introspector = provider.getAccessor(entity.getClass());


        NamedNodeMap attributes = entityElement.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if (!"ormId".equals(attribute.getNodeName())) {
                set(provider, entity, attribute.getNodeName(), attribute.getNodeValue(), references);
            }
        }


        for (Node propertyNode : getChildNodes(entityElement, Node.ELEMENT_NODE)) {
            String propertyName = propertyNode.getNodeName();
            Class propertyType = introspector.getType(propertyName);
            if (propertyType == null)
                throw new ORMUnitNodeProcessingException("no such property: " + propertyName + " in class: " + className);

            if (introspector.isSimpleType(propertyName)) {
                String propertyValue = processSimpleType(provider, propertyNode, introspector, references);
                set(provider, entity, propertyName, propertyValue, references);
                continue;
            } else if (Collection.class.isAssignableFrom(propertyType)) {
                Collection c = processCollection(testSet, propertyNode, introspector, references);
                if (introspector.get(entity, propertyName) != null) {
                    ((Collection) introspector.get(entity, propertyName)).addAll(c);
                } else {
                    introspector.set(entity, propertyName, c);
                }
            } else if (Map.class.isAssignableFrom(propertyType)) {
                Map map = processMap(testSet, propertyNode, introspector, references);
                if (introspector.get(entity, propertyName) != null) {
                    ((Map) introspector.get(entity, propertyName)).putAll(map);
                } else {
                    introspector.set(entity, propertyName, map);
                }
            } else {

                Collection<Node> elemSubNodes = getChildNodes(propertyNode, Node.ELEMENT_NODE);
                Collection<Node> txtSubNodes = getChildNodes(propertyNode, Node.TEXT_NODE, Node.CDATA_SECTION_NODE);
                if (elemSubNodes.size() == 1) {
                    Node referenceNode = elemSubNodes.iterator().next();

                    if ("reference".equals(referenceNode.getNodeName())) {
                        EntityReference entityReference = new EntityReference(propertyName, resolveAndProcessReference(referenceNode, propertyType, references, testSet));
                        references.add(entityReference);
                    } else {
                        Object childEntity = resolveAndProcessEntity(testSet, referenceNode, references);
                        testSet.addCommand(new EntityCommand(extractOrmId(referenceNode),
                                childEntity,
                                testSet.getProvider().getAccessor(entity.getClass()),
                                references));

                        introspector.set(entity, propertyName, childEntity);
                    }
                }
                if (elemSubNodes.size() == 0) {
                    Node childEntityDeclarationNode = txtSubNodes.iterator().next();

                    String propertyValue = childEntityDeclarationNode.getNodeValue().trim();
                    set(provider, entity, propertyName, propertyValue, references);

                }
            }
        }
        return entity;
    }

    /**
     * processes given node and return id as result
     *
     * @param referenceNode <reference /> node. there must be one subnode to each ID property
     * @param type          type of enittiy that this reference points to
     * @param references
     * @param testSet       @return
     */
    public Object resolveAndProcessReference(Node referenceNode, Class type, Set<EntityReference> references, ORMUnitTestSet testSet) {
        ORMProvider provider = testSet.getProvider();
        Class idType = provider.getIdType(type);
        EntityAccessor accessor = provider.getAccessor(type);
        if (accessor.isSimpleType(type)) {

        } else {
            try {
                processEntity(referenceNode, idType.newInstance(), references, testSet);
            } catch (Exception e) {
                throw new ORMUnitFileSyntaxException("cannot extract id properties of id of: " + type.getCanonicalName(), e);
            }
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private Collection<Node> getChildNodes(Node propertyNode, short... nodeTypes) {
        Set<Short> types = new HashSet<Short>();
        for (short s : nodeTypes) {
            types.add(s);
        }

        NodeList childNodes = propertyNode.getChildNodes();
        Collection<Node> nodeList = new LinkedList<Node>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (types.contains(item.getNodeType())) {
                nodeList.add(item);
            }
        }
        return nodeList;
    }

    private String extractOrmId(Node entityElement) {
        String ormId = null;
        NamedNodeMap attributes = entityElement.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);

            if ("ormId".equals(attribute.getNodeName())) {
                ormId = attribute.getNodeValue();
            }
        }

        return ormId;
    }


    private String processSimpleType(ORMProvider provider, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws ORMUnitFileSyntaxException {
        String propertyName = propertyNode.getNodeName();
        String propertyValue = null;
        Collection<Node> nodes = getChildNodes(propertyNode, Node.TEXT_NODE, Node.CDATA_SECTION_NODE, Node.ELEMENT_NODE);
        if (nodes.size() > 1)
            throw new ORMUnitFileSyntaxException("property is allowed to have only one propertyNode: CDATA or  text");

        if (nodes.size() > 0) {
            Node propertyValueNode = nodes.iterator().next();

            if (propertyValueNode != null && (propertyValueNode.getNodeType() == Node.CDATA_SECTION_NODE || propertyValueNode.getNodeType() == Node.TEXT_NODE)) {
                propertyValue = propertyValueNode.getNodeValue().trim();
            }
        }
        return propertyValue;
    }

    private Map processMap(ORMUnitTestSet testSet, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws ORMUnitFileReadException {
        String propertyName = propertyNode.getNodeName();
        Map map = (Map) introspector.newInstance(propertyName);
        Class[] mapParameterTypes = introspector.getMapParameterTypes(propertyName); // {key, value}

        for (Node entryNode : getChildNodes(propertyNode, Node.ELEMENT_NODE)) {

            if (entryNode.getNodeName().equals("entry")) {
                String key = entryNode.getAttributes().getNamedItem("key").getNodeValue();
                for (Node valueNode : getChildNodes(entryNode, Node.ELEMENT_NODE)) {
                    Object element = resolveAndProcessEntity(testSet, valueNode, references);

                    if (!mapParameterTypes[1].isAssignableFrom(element.getClass())) {
                        throw new ORMUnitNodeProcessingException(valueNode.getNodeName() + "(" + element.getClass().getCanonicalName() + ") is not subclass of collection parameter typ: " + mapParameterTypes[1].getCanonicalName());
                    }

                    try {
                        map.put(ORMUnitHelper.convert(mapParameterTypes[0], key),
                                element);
                    } catch (ConvertionException e) {
                        throw new ORMUnitFileReadException("cannot convert: " + key + " to desired type: " + mapParameterTypes[0].getCanonicalName(), e);
                    }

                }
            }
        }
        return map;
    }


    private Collection processCollection(ORMUnitTestSet testSet, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws ORMUnitFileReadException {
        String propertyName = propertyNode.getNodeName();

        Collection c = (Collection) introspector.newInstance(propertyName);
        Class collectionParameterType = introspector.getCollectionParameterType(propertyName);

        for (Node elementNode : getChildNodes(propertyNode, Node.ELEMENT_NODE)) {
            Object element = resolveAndProcessEntity(testSet, elementNode, references);
            if (!collectionParameterType.isAssignableFrom(element.getClass())) {
                throw new ORMUnitNodeProcessingException(elementNode.getNodeName() + "(" + element.getClass().getCanonicalName() + ") is not subclass of collection parameter typ: " + collectionParameterType.getCanonicalName());
            }

            c.add(element);
        }
        return c;

    }

    private Object resolveAndProcessEntity(ORMUnitTestSet testset, Node valueNode, Set<EntityReference> references) throws ORMUnitFileReadException {
        NodeProcessor nodeProcessor = testset.getNodeProcessor(valueNode.getNodeName());

        Object element = null;
        if (nodeProcessor instanceof EntityNodeProcessor) {
            Class entityClass = null;
            try {
                entityClass = ((EntityNodeProcessor) nodeProcessor).getEntityClass();
            } catch (ClassNotFoundException e) {
                throw new ORMUnitFileReadException(e);
            }


            try {
                element = entityClass.newInstance();
                processEntity(valueNode, element, references, testset);
            } catch (InstantiationException e) {
                throw new ORMUnitFileReadException(e);
            } catch (IllegalAccessException e) {
                throw new ORMUnitFileReadException(e);
            }
        } else
            throw new ORMUnitFileSyntaxException("Cannot find entitynodeprocess for node of type: " + valueNode.getNodeName());

        return element;
    }


    private void set(ORMProvider provider, Object entity, String propertyName, String value, Set<EntityReference> references) {
        EntityAccessor introspector = provider.getAccessor(entity.getClass());
        try {
            Class type = introspector.getType(propertyName);
            if (type == null)
                throw new ORMEntityAccessException("Property: " + propertyName + " does not exists for entity: " + entity.getClass().getCanonicalName());
            if (value != null && value.matches(ReferencePattern)) {
                value = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")")).trim();
                references.add(new EntityReference(
                        propertyName,
                        ORMUnitHelper.convert(
                                provider.getIdType(type),
                                value)));

            } else if (value != null && value.matches(ORMReferencePattern)) {
                value = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")")).trim();
                references.add(new EntityReference(
                        propertyName,
                        value,
                        EntityReference.Type.ORMUNIT));
            } else {
                introspector.set(entity, propertyName, ORMUnitHelper.convert(
                        type,
                        value));
            }
        } catch (ConvertionException e) {
            throw new ORMUnitNodeProcessingException(e);
        }
    }

}
