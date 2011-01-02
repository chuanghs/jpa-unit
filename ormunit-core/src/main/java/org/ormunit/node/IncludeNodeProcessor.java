package org.ormunit.node;

import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitConfiguration;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 00:01
 */
public class IncludeNodeProcessor implements INodeProcessor {


    public void process(Node jpaUnitElement, ORMUnitConfiguration result, ORMUnit reader) throws ORMUnitNodeProcessingException {
        Node srcNode = jpaUnitElement.getAttributes().getNamedItem("src");
        if (srcNode != null) {
            try {
                include(srcNode.getNodeValue().trim(), reader, result);
            } catch (ORMUnitFileReadException e) {
                throw new ORMUnitNodeProcessingException(e);
            }
        }
    }

    public void include(String s, ORMUnit configurationReader, ORMUnitConfiguration configuration) throws ORMUnitFileReadException {
        configurationReader.read(s, configuration);
    }
}
