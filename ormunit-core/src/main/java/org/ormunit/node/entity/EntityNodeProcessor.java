package org.ormunit.node.entity;

import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitHelper;
import org.ormunit.TestSet;
import org.ormunit.command.EntityCommand;
import org.ormunit.command.EntityReference;
import org.ormunit.exception.ConvertionException;
import org.ormunit.exception.FileReadException;
import org.ormunit.exception.FileSyntaxException;
import org.ormunit.exception.NodeProcessingException;
import org.ormunit.node.NodeProcessor;
import org.ormunit.node.entity.accessor.EntityAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:27
 */
public class    EntityNodeProcessor extends NodeProcessor {

    public static final String ReferencePattern = "ref\\(.+\\)";
    public static final String ORMReferencePattern = "ormref\\(.+\\)";

    private static final Logger log = LoggerFactory.getLogger(EntityNodeProcessor.class);
    private static final ThreadLocal<Stack<Class<?>>> threadLocal = new ThreadLocal<Stack<Class<?>>>();

    static {
        threadLocal.set(new Stack<Class<?>>());
    }

    private final Class<?> entityClass;

    public EntityNodeProcessor(Class<?> entityClass) {
        super(null);
        this.entityClass = entityClass;
    }

    public synchronized void process(Node node, TestSet testSet) throws NodeProcessingException {
        Set<EntityReference> references = new HashSet<EntityReference>();

        try {
            tryAddCommand(entityClass, node, testSet, references);
        } catch (FileReadException e) {
            throw new NodeProcessingException(e);
        }

    }

    private Object tryAddCommand(Class<?> entityClass, Node node, TestSet testSet, Set<EntityReference> references) throws FileReadException {
        Object entity = null;
        testSet.addCommand(new EntityCommand(
                extractOrmId(node),
                entity = processEntity(node, instantiate(entityClass), references, testSet),
                getAccessor(entityClass, testSet.getProvider()),
                references));
        return entity;
    }

    private EntityAccessor getAccessor(Class<?> entityClass, ORMProvider provider) {
        return provider.getAccessor(entityClass, threadLocal.get().isEmpty() ? null : threadLocal.get().peek());
    }

    private Object instantiate(Class<?> entityClass) {
        try {
            Constructor<?> constructor = entityClass.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new NodeProcessingException(String.format("Cannot instantiate abstract class %s.", entityClass.getCanonicalName()), e);
        } catch (NoSuchMethodException e) {
            throw new NodeProcessingException(String.format("Cannot instantiate class %s. There default constructor on that class.", entityClass.getCanonicalName()), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("This should never happen because setAccessible(true) is invoked on default constructor.");
        } catch (InvocationTargetException e) {
            throw new NodeProcessingException(String.format("Constructor %s.<init>() threw an exception.", entityClass.getCanonicalName()), e);
        }
    }


    public Object processEntity(Node entityElement, Object entity, Set<EntityReference> references, TestSet testSet) throws FileReadException {
        ORMProvider provider = testSet.getProvider();
        EntityAccessor accessor = getAccessor(entity.getClass(), provider);

        threadLocal.get().push(entity.getClass());
        try {
            NamedNodeMap attributes = entityElement.getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String propertyName = attribute.getNodeName();
                String propertyValue = attribute.getNodeValue();
                if (!"ormId".equals(propertyName)) {
                    if (isReference(propertyValue)) {
                        setReference(provider, entity, propertyName, propertyValue, references);
                    } else {
                        setSimpleType(provider, entity, propertyName, propertyValue);
                    }
                }
            }


            for (Node propertyNode : getChildNodes(entityElement, Node.ELEMENT_NODE)) {
                String propertyName = propertyNode.getNodeName();
                Class propertyType = accessor.getType(propertyName);

                if (isSimpleType(accessor.getType(propertyName))) {
                    String propertyValue = processSimpleType(testSet, propertyNode, accessor, references);
                    setSimpleType(provider, entity, propertyName, propertyValue);
                } else if (isCollection(propertyType)) {
                    Collection value = processCollection(testSet, propertyNode, accessor, references);
                    accessor.set(entity, propertyName, value);
                } else if (isMap(propertyType)) {
                    Map value = processMap(testSet, propertyNode, accessor, references);
                    accessor.set(entity, propertyName, value);
                } else if (isEmbeddable(provider, propertyType)) {
                    Object value = processEmbeddable(references, testSet, accessor, propertyNode, propertyName);
                    accessor.set(entity, propertyName, value);
                } else {
                    Collection<Node> elemSubNodes = getChildNodes(propertyNode, Node.ELEMENT_NODE);
                    Collection<Node> txtSubNodes = getChildNodes(propertyNode, Node.TEXT_NODE, Node.CDATA_SECTION_NODE);
                    if (elemSubNodes.size() == 1) {
                        Node referenceNode = elemSubNodes.iterator().next();

                        if ("reference".equals(referenceNode.getNodeName())) {
                            EntityReference entityReference = new EntityReference(propertyName, resolveAndProcessReference(referenceNode, propertyType, references, testSet), EntityReference.ReferenceType.DB);
                            references.add(entityReference);
                        } else {
                            accessor.set(entity, propertyName, tryAddCommand(resolveEntityType(testSet, referenceNode), referenceNode, testSet, references));
                        }
                    } else if (elemSubNodes.size() == 0 && txtSubNodes.size() > 0 && isReference(txtSubNodes.iterator().next().getNodeValue().trim())) {
                        setReference(provider, entity, propertyName, txtSubNodes.iterator().next().getNodeValue().trim(), references);

                    }
                }
            }
            return entity;
        } finally {
            threadLocal.get().pop();
        }
    }

    private boolean isCollection(Class propertyType) {
        return Collection.class.isAssignableFrom(propertyType);
    }

    private boolean isMap(Class propertyType) {
        return Map.class.isAssignableFrom(propertyType);
    }

    private boolean isEmbeddable(ORMProvider provider, Class propertyType) {
        return provider.isEmbeddable(propertyType);
    }

    private Object processEmbeddable(Set<EntityReference> references, TestSet testSet, EntityAccessor accessor, Node propertyNode, String propertyName) throws FileReadException {
        return processEntity(propertyNode, accessor.newInstance(propertyName), references, testSet);
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
        Class idType = testSet.getProvider().getIdType(referencedType);
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
            try {
                return processEntity(referenceNode, instantiate(idType), references, testSet);
            } catch (FileReadException e) {
                throw new FileSyntaxException("cannot extract id properties of id of: " + referencedType.getCanonicalName(), e);
            }
        }
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

    private String processSimpleType(TestSet testSet, Node propertyNode, EntityAccessor accessor, Set<EntityReference> references) throws FileSyntaxException {
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

    private Map processMap(TestSet testSet, Node propertyNode, EntityAccessor accessor, Set<EntityReference> references) throws FileReadException {
        String propertyName = propertyNode.getNodeName();
        Map map = (Map) accessor.newInstance(propertyName);
        Class[] mapParameterTypes = accessor.getMapParameterTypes(propertyName); // {key, value}

        for (Node entryNode : getChildNodes(propertyNode, Node.ELEMENT_NODE)) {

            if (entryNode.getNodeName().equals("entry")) {
                String key = entryNode.getAttributes().getNamedItem("key").getNodeValue();
                for (Node valueNode : getChildNodes(entryNode, Node.ELEMENT_NODE)) {
                    Class<?> referencedEntityType = resolveEntityType(testSet, valueNode);

                    if (!mapParameterTypes[1].isAssignableFrom(referencedEntityType)) {
                        throw new NodeProcessingException(valueNode.getNodeName() + "(" + referencedEntityType.getCanonicalName() + ") is not subclass of collection parameter typ: " + mapParameterTypes[1].getCanonicalName());
                    }

                    try {
                        map.put(ORMUnitHelper.convert(mapParameterTypes[0], key), tryAddCommand(referencedEntityType, valueNode, testSet, references));
                    } catch (ConvertionException e) {
                        throw new FileReadException("cannot convert: " + key + " to desired type: " + mapParameterTypes[0].getCanonicalName(), e);
                    }
                }
            }
        }
        return map;
    }

    private Collection processCollection(TestSet testSet, Node propertyNode, EntityAccessor accessor, Set<EntityReference> references) throws FileReadException {
        String propertyName = propertyNode.getNodeName();

        Collection c = (Collection) accessor.newInstance(propertyName);
        Class collectionParameterType = accessor.getCollectionParameterType(propertyName);

        for (Node elementNode : getChildNodes(propertyNode, Node.ELEMENT_NODE)) {
            Class<?> referencedEntityType = resolveEntityType(testSet, elementNode);
            if (!collectionParameterType.isAssignableFrom(referencedEntityType)) {
                throw new NodeProcessingException(elementNode.getNodeName() + "(" + referencedEntityType.getCanonicalName() + ") is not subclass of collection parameter typ: " + collectionParameterType.getCanonicalName());
            }
            c.add(tryAddCommand(referencedEntityType, elementNode, testSet, references));
        }
        return c;

    }

    private Class<?> resolveEntityType(TestSet testset, Node valueNode) throws FileReadException {
        NodeProcessor nodeProcessor = testset.getNodeProcessor(valueNode.getNodeName());

        if (nodeProcessor instanceof EntityNodeProcessor) {
            return ((EntityNodeProcessor) nodeProcessor).entityClass;
        } else
            throw new FileSyntaxException(String.format("Property node contains node \"%s\" that is no entity node", valueNode.getNodeName()));
    }

    private boolean isReference(String value) {
        return value != null && (value.matches(ReferencePattern) || value.matches(ORMReferencePattern));  //To change body of created methods use File | Settings | File Templates.
    }

    private void setReference(ORMProvider provider, Object entity, String propertyName, String value, Set<EntityReference> references) throws ConvertionException {
        String idValue = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")")).trim();
        EntityReference reference = null;
        if (value.matches(ReferencePattern)) {
            reference = new EntityReference(propertyName, ORMUnitHelper.convert(provider.getIdType(entity.getClass()), idValue), EntityReference.ReferenceType.DB);
        } else if (value.matches(ORMReferencePattern)) {
            reference = new EntityReference(propertyName, idValue, EntityReference.ReferenceType.ORMUNIT);
        }
        references.add(reference);
    }

    private void setSimpleType(ORMProvider provider, Object entity, String propertyName, String value) throws ConvertionException {
        EntityAccessor accessor = getAccessor(entity.getClass(), provider);
        accessor.set(entity, propertyName, ORMUnitHelper.convert(accessor.getType(propertyName), value));
    }


}
