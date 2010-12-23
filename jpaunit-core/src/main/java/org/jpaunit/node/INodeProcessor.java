package org.jpaunit.node;

import org.jpaunit.JPAUnitConfiguration;
import org.jpaunit.exception.JPAUnitFileSyntaxException;
import org.jpaunit.exception.JPAUnitNodeProcessingException;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:12
 */
public interface INodeProcessor {

    void process(Node jpaUnitElement, JPAUnitConfiguration result) throws JPAUnitNodeProcessingException;

}
