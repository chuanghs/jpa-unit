package org.ormunit;

import org.ormunit.entity.EntityAccessor;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:33
 */
public interface ORMProvider {

    EntityAccessor getAccessor(Class<?> entityClass);

    Class getIdType(Class<?> entityClass);

    Object getId(Object entity) throws Exception;

    void setId(Object entity, Object id) throws Exception;

    Object entity(Object entity);

    void statement(String statement);

    <T extends Object> T getEntity(Class<T> entityClass, Object id);

    Set<Class<?>> getManagedTypes();
}
