package org.jpaunit.node;

import org.jpaunit.JPAUnitConfiguration;
import org.jpaunit.exception.JPAUnitFileSyntaxException;
import org.jpaunit.exception.JPAUnitNodeProcessingException;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:27
 */
public class EntityNodeProcessor implements INodeProcessor {

    private final String className;

    public EntityNodeProcessor(String className){
        this.className = className;
    }
    public String getClassName() {
        return className;
    }


    public void process(Node jpaUnitElement, JPAUnitConfiguration result) throws JPAUnitNodeProcessingException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
