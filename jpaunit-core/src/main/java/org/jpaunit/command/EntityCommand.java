package org.jpaunit.command;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class EntityCommand extends JPAUnitCommand {

    private Object entity;

    public EntityCommand(Object entity) {

        this.entity = entity;
    }

    @Override
    public void visit(JPAUnitCommandVisitor visitor) {
        visitor.entity(this.entity);
    }
}
