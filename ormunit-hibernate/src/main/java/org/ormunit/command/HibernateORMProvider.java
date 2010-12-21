package org.ormunit.command;

import org.hibernate.Session;
import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitIntrospector;

import java.beans.IntrospectionException;

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

    public Class<?> getIdType(Class<?> propertyType) {
        try {
            return ORMUnitIntrospector.getInspector(propertyType).getPropertyType(
                    session.getSessionFactory().getClassMetadata(propertyType).getIdentifierPropertyName());
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public void entity(Object entity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void statement(String statement) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T> T getReference(Class<T> propertyClass, Object id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
