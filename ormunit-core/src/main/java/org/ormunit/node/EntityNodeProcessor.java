package org.ormunit.node;

import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitHelper;
import org.ormunit.TestSet;
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

    private static ThreadLocal<Stack<Class<?>>> threadLocal = new ThreadLocal<Stack<Class<?>>>();


    public EntityNodeProcessor(String className) {
        super(null);
        this.className = className;
    }

    public Class getEntityClass() throws ClassNotFoundException {
        return Class.forName(className);
    }

    public synchronized void process(Node node, TestSet testSet) throws NodeProcessingException {

        threadLocal.set(new Stack<Class<?>>());
        Object entity = null;
        try {
            entity = getEntityClass().newInstance();

            Set<EntityReference> references = new HashSet<EntityReference>();

            entity = processEntity(node, entity, references, testSet);
            String ormId = extractOrmId(node);
            EntityAccessor accessor = testSet.getProvider().getAccessor(entity.getClass(), null);

            testSet.addCommand(new EntityCommand(ormId,
                    entity,
                    accessor,
                    references));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new NodeProcessingException(e);
        }

    }


    public Object processEntity(Node entityElement, Object entity, Set<EntityReference> references, TestSet testSet) throws FileReadException {
        ORMProvider provider = testSet.getProvider();
        EntityAccessor accessor = provider.getAccessor(entity.getClass(), threadLocal.get().isEmpty() ? null : threadLocal.get().peek());

        threadLocal.get().push(entity.getClass());
        try {
            NamedNodeMap attributes = entityElement.getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (!"ormId".equals(attribute.getNodeName())) {
                    set(provider, entity, attribute.getNodeName(), attribute.getNodeValue(), references);
                }
            }


            for (Node propertyNode : getChildNodes(entityElement, Node.ELEMENT_NODE)) {
                String propertyName = propertyNode.getNodeName();
                Class propertyType = accessor.getType(propertyName);
                if (propertyType == null)
                    throw new NodeProcessingException("no such property: " + propertyName + " in class: " + className);

                if (isSimpleType(accessor.getType(propertyName))) {
                    String propertyValue = processSimpleType(provider, propertyNode, accessor, references);
                    set(provider, entity, propertyName, propertyValue, references);
                    continue;
                } else if (Collection.class.isAssignableFrom(propertyType)) {
                    Collection c = processCollection(testSet, propertyNode, accessor, references);
                    if (accessor.get(entity, propertyName) != null) {
                        ((Collection) accessor.get(entity, propertyName)).addAll(c);
                    } else {
                        accessor.set(entity, propertyName, c);
                    }
                } else if (Map.class.isAssignableFrom(propertyType)) {
                    Map map = processMap(testSet, propertyNode, accessor, references);
                    if (accessor.get(entity, propertyName) != null) {
                        ((Map) accessor.get(entity, propertyName)).putAll(map);
                    } else {
                        accessor.set(entity, propertyName, map);
                    }
                } else if (provider.isEmbeddable(propertyType)) {
                    accessor.set(entity, propertyName, processEntity(propertyNode, accessor.newInstance(propertyName), references, testSet));
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
                                    testSet.getProvider().getAccessor(entity.getClass(), null),
                                    references));

                            accessor.set(entity, propertyName, childEntity);
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
        } finally {
            threadLocal.get().pop();
        }
    }

    private boolean isSimpleType(Class type) {
        return EntityAccessor.simpleTypes.contains(type);
    }

    /**
     * processes given node and return id as a result
     *
     * @param referenceNode  <reference /> node. there must be one subnode to each ID property
     * @param referencedType type of enittiy that this reference points to
     * @param references
     * @param testSet        @return
     */
    public Object resolveAndProcessReference(Node referenceNode, Class referencedType, Set<EntityReference> references, TestSet testSet) throws FileSyntaxException {
        ORMProvider provider = testSet.getProvider();
        Class idType = provider.getIdType(referencedType);
        EntityAccessor accessor = provider.getAccessor(referencedType, null);
        if (isSimpleType(idType)) {  // true if id is of simple type
            Collection<Node> validTypeChildNodes = getChildNodes(referenceNode, Node.TEXT_NODE, Node.CDATA_SECTION_NODE);
            NodeList allChildNodes = referenceNode.getChildNodes();
            if (validTypeChildNodes.size() == 1 && allChildNodes.getLength() == 1) {
                try {
                    return ORMUnitHelper.convert(idType, validTypeChildNodes.iterator().next().getNodeValue());
                } catch (ConvertionException e) {
                    throw new NodeProcessingException(e);
                }
            } else {
                throw new FileSyntaxException("reference to: " + referencedType.getCanonicalName() + " can only be declared by simple value");
            }
        } else { // if id simple compound then process this node as simple POJO
            Object referenceId = null;
            try {
                referenceId = idType.newInstance();
            } catch (Exception e) {
                throw new FileSyntaxException("cannot instantiate id (" + idType.getCanonicalName() + ") of: " + referencedType.getCanonicalName() + ". Is there default constructor?", e);
            }
            try {
                return processEntity(referenceNode, referenceId, references, testSet);
            } catch (FileReadException e) {
                throw new FileSyntaxException("cannot extract id properties of id of: " + referencedType.getCanonicalName(), e);
            }
        }
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


    private String processSimpleType(ORMProvider provider, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws FileSyntaxException {
        String propertyName = propertyNode.getNodeName();
        String propertyValue = null;
        Collection<Node> nodes = getChildNodes(propertyNode, Node.TEXT_NODE, Node.CDATA_SECTION_NODE, Node.ELEMENT_NODE);
        if (nodes.size() > 1)
            throw new FileSyntaxException("property is allowed to have only one propertyNode: CDATA or  text");

        if (nodes.size() > 0) {
            Node propertyValueNode = nodes.iterator().next();

            if (propertyValueNode != null && (propertyValueNode.getNodeType() == Node.CDATA_SECTION_NODE || propertyValueNode.getNodeType() == Node.TEXT_NODE)) {
                propertyValue = propertyValueNode.getNodeValue().trim();
            }
        }
        return propertyValue;
    }

    private Map processMap(TestSet testSet, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws FileReadException {
        String propertyName = propertyNode.getNodeName();
        Map map = (Map) introspector.newInstance(propertyName);
        Class[] mapParameterTypes = introspector.getMapParameterTypes(propertyName); // {key, value}

        for (Node entryNode : getChildNodes(propertyNode, Node.ELEMENT_NODE)) {

            if (entryNode.getNodeName().equals("entry")) {
                String key = entryNode.getAttributes().getNamedItem("key").getNodeValue();
                for (Node valueNode : getChildNodes(entryNode, Node.ELEMENT_NODE)) {
                    Object element = resolveAndProcessEntity(testSet, valueNode, references);

                    if (!mapParameterTypes[1].isAssignableFrom(element.getClass())) {
                        throw new NodeProcessingException(valueNode.getNodeName() + "(" + element.getClass().getCanonicalName() + ") is not subclass of collection parameter typ: " + mapParameterTypes[1].getCanonicalName());
                    }

                    try {
                        map.put(ORMUnitHelper.convert(mapParameterTypes[0], key),
                                element);
                    } catch (ConvertionException e) {
                        throw new FileReadException("cannot convert: " + key + " to desired type: " + mapParameterTypes[0].getCanonicalName(), e);
                    }
                }
            }
        }
        return map;
    }


    private Collection processCollection(TestSet testSet, Node propertyNode, EntityAccessor introspector, Set<EntityReference> references) throws FileReadException {
        String propertyName = propertyNode.getNodeName();

        Collection c = (Collection) introspector.newInstance(propertyName);
        Class collectionParameterType = introspector.getCollectionParameterType(propertyName);

        for (Node elementNode : getChildNodes(propertyNode, Node.ELEMENT_NODE)) {
            Object element = resolveAndProcessEntity(testSet, elementNode, references);
            if (!collectionParameterType.isAssignableFrom(element.getClass())) {
                throw new NodeProcessingException(elementNode.getNodeName() + "(" + element.getClass().getCanonicalName() + ") is not subclass of collection parameter typ: " + collectionParameterType.getCanonicalName());
            }

            c.add(element);
        }
        return c;

    }

    private Object resolveAndProcessEntity(TestSet testset, Node valueNode, Set<EntityReference> references) throws FileReadException {
        NodeProcessor nodeProcessor = testset.getNodeProcessor(valueNode.getNodeName());

        Object entity = null;
        if (nodeProcessor instanceof EntityNodeProcessor) {
            Class entityClass = null;
            try {
                entityClass = ((EntityNodeProcessor) nodeProcessor).getEntityClass();
            } catch (ClassNotFoundException e) {
                throw new FileReadException(e);
            }


            try {
                entity = entityClass.newInstance();
                processEntity(valueNode, entity, references, testset);
            } catch (InstantiationException e) {
                throw new FileReadException(e);
            } catch (IllegalAccessException e) {
                throw new FileReadException(e);
            }
        } else
            throw new FileSyntaxException("Cannot find entitynodeprocess for node of type: " + valueNode.getNodeName());

        return entity;
    }


    private void set(ORMProvider provider, Object entity, String propertyName, String value, Set<EntityReference> references) {
        EntityAccessor introspector = provider.getAccessor(entity.getClass(), null);
        try {
            Class type = introspector.getType(propertyName);
            if (type == null)
                throw new EntityAccessException("Property: " + propertyName + " does not exists for entity: " + entity.getClass().getCanonicalName());
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
            throw new NodeProcessingException(e);
        }
    }

}
