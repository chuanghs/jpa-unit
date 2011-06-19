package org.ormunit.node;

import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.w3c.dom.Node;

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

    public abstract void process(Node jpaUnitElement, TestSet result) throws ORMUnitNodeProcessingException;

}
