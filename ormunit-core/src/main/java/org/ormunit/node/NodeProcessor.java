package org.ormunit.node;

import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.exception.NodeProcessingException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:12
 */
public abstract class NodeProcessor {

    private final ORMUnitPropertiesReader ormUnit;

    public NodeProcessor(ORMUnitPropertiesReader ormUnit){
        this.ormUnit = ormUnit;
    }

    public ORMUnitPropertiesReader getOrmUnit() {
        return ormUnit;
    }

    public abstract void process(Node jpaUnitElement, TestSet result) throws NodeProcessingException;

    protected Collection<Node> getChildNodes(Node propertyNode, Short... nodeTypes) {
        Set<Short> types = new HashSet<Short>(Arrays.asList(nodeTypes));

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
}
