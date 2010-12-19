package org.jpaunit.command;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class StatementCommand extends JPAUnitCommand {

    private String statement;

    public StatementCommand(String statement) {

        this.statement = statement;
    }

    @Override
    public void visit(JPAUnitCommandVisitor visitor) {
        visitor.statement(statement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatementCommand that = (StatementCommand) o;

        if (statement != null ? !statement.equals(that.statement) : that.statement != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return statement != null ? statement.hashCode() : 0;
    }
}
