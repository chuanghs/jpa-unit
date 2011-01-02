package org.ormunit.node;

import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitConfiguration;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:12
 */
public interface INodeProcessor {

    void process(Node jpaUnitElement, ORMUnitConfiguration result, ORMUnit reader) throws ORMUnitNodeProcessingException;

}
