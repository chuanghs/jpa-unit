package org.ormunit.command;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 21.12.10
 * Time: 08:48
 */
public interface ORMCommandVisitor {

     public void entity(Object entity);

    public void statement(String statement);

    <T> T getReference(Class<T> propertyClass, Object id);
}
