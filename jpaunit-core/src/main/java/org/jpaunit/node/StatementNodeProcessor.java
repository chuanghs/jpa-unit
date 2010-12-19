package org.jpaunit.node;

import org.jpaunit.JPAUnitConfiguration;
import org.jpaunit.JPAUnitConfigurationReader;
import org.jpaunit.command.StatementCommand;
import org.jpaunit.exception.JPAUnitNodeProcessingException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:38
 */
public class StatementNodeProcessor implements INodeProcessor {

    public void process(Node jpaUnitElement, JPAUnitConfiguration result, JPAUnitConfigurationReader reader) throws JPAUnitNodeProcessingException {
        NodeList statementChildren = jpaUnitElement.getChildNodes();
        if (statementChildren.getLength() > 1) {
            throw new JPAUnitNodeProcessingException("statement element is allowed only to have 1 child: CDATA or text");
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
