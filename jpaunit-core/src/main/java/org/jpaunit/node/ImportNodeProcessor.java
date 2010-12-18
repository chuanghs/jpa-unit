package org.jpaunit.node;

import org.jpaunit.JPAUnitConfiguration;
import org.jpaunit.JPAUnitConfigurationReader;
import org.jpaunit.exception.JPAUnitNodeProcessingException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:37
 */
public class ImportNodeProcessor implements INodeProcessor {


    public void process(Node jpaUnitElement, JPAUnitConfiguration result, JPAUnitConfigurationReader reader) throws JPAUnitNodeProcessingException {
        NamedNodeMap importAttributes = jpaUnitElement.getAttributes();
        Node classNode = importAttributes.getNamedItem("class");
        Node aliasNode = importAttributes.getNamedItem("alias");

        if (classNode == null) {
            throw new JPAUnitNodeProcessingException("import element is must have \"class\" attribute. It must be fully qualified class name");
        }
        String className = classNode.getNodeValue();
        int dotIndex = className.lastIndexOf(".");
        String alias = className.substring(dotIndex > -1 ? dotIndex + 1 : 0);
        if (aliasNode != null) {
            alias = aliasNode.getNodeValue();
        }
        result.addImport(className, alias);
        reader.registerNodeProcessor(alias, new EntityNodeProcessor(className));
    }
}
