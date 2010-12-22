package org.ormunit.command;

import org.hibernate.Session;
import org.ormunit.ORMProvider;
import org.ormunit.entity.EntityAccessor;

import java.beans.IntrospectionException;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 21.12.10
 * Time: 21:08
 */
public class HibernateORMProvider implements ORMProvider {

    private final Session session;

    public HibernateORMProvider(Session session) {
        this.session = session;
    }

    public Class<?> getIdType(Class<?> entityType) {
        return session.getSessionFactory().getClassMetadata(entityType).getIdentifierType().getReturnedClass();
    }

    public void entity(Object entity) {
        session.persist(entity);
    }

    public void statement(String statement) {
        session.createSQLQuery(statement).executeUpdate();
    }

    public <T> T getReference(Class<T> propertyClass, Object id) {
        return (T) session.get(propertyClass, (Serializable) id);
    }

    public EntityAccessor getAccessor(Class<?> aClass) throws IntrospectionException {
        return null;
    }
}
