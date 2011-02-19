package org.ormunit.node;

import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:12
 */
public abstract class NodeProcessor {

    private final ORMUnit ormUnit;

    public NodeProcessor(ORMUnit ormUnit){
        this.ormUnit = ormUnit;
    }

    public ORMUnit getOrmUnit() {
        return ormUnit;
    }

    public abstract void process(Node jpaUnitElement, ORMUnitTestSet result) throws ORMUnitNodeProcessingException;

}
