package org.ormunit;

import org.ormunit.entity.EntityAccessor;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:33
 */
public interface ORMProvider {

    Class getIdType(Class<?> entityClass);

    void entity(Object entity);

    void statement(String statement);

    <T> T getReference(Class<T> propertyClass, Object id);

    EntityAccessor getAccessor(Class<?> aClass);
}
