package org.ormunit;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 21.12.10
 * Time: 21:08
 */
public class HibernateORMProvider extends AORMProvider {

    private static final Logger log = LoggerFactory.getLogger(HibernateORMProvider.class);

    private final Session session;
    private final boolean selfManagedSession;

    private ORMUnit ormUnit;
    private String unitName;
    private Properties persistenceContextProperties;

    public HibernateORMProvider(Session session) {
        this.session = session;
        this.selfManagedSession = false;
    }

    public HibernateORMProvider(ORMUnit ormUnit, String unitName) {
        this.ormUnit = ormUnit;
        this.unitName = unitName;
        this.persistenceContextProperties = ormUnit.getDefaultDataSourceProperties();
        this.session = null;
        this.selfManagedSession = true;

    }

    public boolean isPropertyAccessed(Class clazz) {
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public boolean isFieldAccessed(Class clazz) {
        while (clazz != null) {
            for (Field m : clazz.getDeclaredFields()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public Class<?> getIdType(Class<?> entityType) {
        return session.getSessionFactory().getClassMetadata(entityType).getIdentifierType().getReturnedClass();
    }

    public void entity(Object entity) {
        session.saveOrUpdate(entity);
    }

    public void statement(String statement) {
        session.createSQLQuery(statement).executeUpdate();
    }

    public <T> T getReference(Class<T> entityClass, Object id) {
        return (T) session.get(entityClass, (Serializable) id);
    }

    public void setUp() {
        session.getTransaction().begin();
    }

    public void tearDown() {
        session.getTransaction().rollback();
        session.close();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                persistenceContextProperties.getProperty("connection.url"),
                persistenceContextProperties.getProperty("connection.username"),
                persistenceContextProperties.getProperty("connection.password"));
    }
}
