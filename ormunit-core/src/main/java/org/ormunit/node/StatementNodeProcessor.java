package org.ormunit.node;

import org.ormunit.TestSet;
import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.command.StatementCommand;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:38
 */
public class StatementNodeProcessor extends NodeProcessor {



    public StatementNodeProcessor(ORMUnitPropertiesReader ormUnit){
        super(ormUnit);
    }

    public void process(Node jpaUnitElement, TestSet result) throws ORMUnitNodeProcessingException {
        NodeList statementChildren = jpaUnitElement.getChildNodes();
        if (statementChildren.getLength() > 1) {
            throw new ORMUnitNodeProcessingException("statement element is allowed only to have 1 child: CDATA or text");
        }
        String statement = null;

        Node statementCDATA = statementChildren.item(0);
        if (statementCDATA != null && (statementCDATA.getNodeType() == Node.CDATA_SECTION_NODE || statementCDATA.getNodeType() == Node.TEXT_NODE)) {
            statement = statementCDATA.getNodeValue();
        }
        if (statement == null) {
            NamedNodeMap attributes = jpaUnitElement.getAttributes();
            Node codeNode = attributes.getNamedItem("code");
            if (codeNode != null)
                statement = codeNode.getNodeValue();

        }
        if (statement != null)
            result.addCommand(new StatementCommand(statement));
    }
}
