package org.jpaunit.command;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class JPAUnitCommand {

    public abstract void visit(JPAUnitCommandVisitor visitor);
}
