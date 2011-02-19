package org.ormunit.node;

import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 18.12.10
 * Time: 17:03
 */
public class FakeNodeProcessor extends NodeProcessor {

    public FakeNodeProcessor(ORMUnit ormUnit) {
        super(ormUnit);
    }

    public void process(Node jpaUnitElement, ORMUnitTestSet result) throws ORMUnitNodeProcessingException {

    }
}
