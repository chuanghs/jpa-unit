package org.ormunit;

import org.ormunit.entity.EntityAccessor;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:33
 */
public interface ORMProvider {

    EntityAccessor getAccessor(Class<?> entityClass);

    Class getIdType(Class<?> entityClass);

    void entity(Object entity);

    void statement(String statement);

    <T> T getReference(Class<T> entityClass, Object id);

    Class getCollectionParameterType(Class<?> entityClass, String propertyName);
}
