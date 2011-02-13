package org.ormunit.node;

import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 00:01
 */
public class IncludeNodeProcessor extends ANodeProcessor {




    public IncludeNodeProcessor(ORMUnit ormUnit){
        super(ormUnit);
    }


    public void process(Node jpaUnitElement, ORMUnitTestSet result) throws ORMUnitNodeProcessingException {
        Node srcNode = jpaUnitElement.getAttributes().getNamedItem("src");
        if (srcNode != null) {
            try {
                include(srcNode.getNodeValue().trim(), result);
            } catch (ORMUnitFileReadException e) {
                throw new ORMUnitNodeProcessingException(e);
            }
        }
    }

    public void include(String s, ORMUnitTestSet testSet) throws ORMUnitFileReadException {
        getOrmUnit().read(s, testSet);
    }
}
