package org.ormunit;

import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.ormunit.junit.HibernateHelper;
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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 21.12.10
 * Time: 21:08
 */
public class HibernateORMProvider extends ORMProviderAdapter {

    private static final Logger log = LoggerFactory.getLogger(HibernateORMProvider.class);

    private Session session;
    private SessionFactory sessionFactory;

    private String unitName;
    private ORMUnitPropertiesReader ormUnit;
    private Properties persistenceContextProperties;


    public HibernateORMProvider(ORMUnitPropertiesReader ormUnit, String unitName) {
        this.ormUnit = ormUnit;
        Configuration configuration = new Configuration();
        this.sessionFactory = configuration
                .mergeProperties(flatten(ormUnit.getDefaultDataSourceProperties(HibernateHelper.hibernateConnection)))
                .configure(ormUnit.getWorkClass().getResource(unitName))
                .buildSessionFactory();
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

    public Object getId(Object entity) throws Exception {
        return session.getIdentifier(entity);
    }

    public void setId(Object entity, Object id) throws Exception {
        this.sessionFactory.getClassMetadata(entity.getClass()).setIdentifier(entity, (Serializable) id, EntityMode.POJO);
    }

    public Object entity(Object entity) {
        session.saveOrUpdate(entity);
        return entity;
    }

    public void statement(String statement) {
        session.createSQLQuery(statement).executeUpdate();
    }

    public <T> T getEntity(Class<T> entityClass, Object id) {
        return (T) session.get(entityClass, (Serializable) id);
    }

    public Set<Class<?>> getManagedTypes() {
        return new HashSet<Class<?>>();
    }

    public void setUp() {
        this.session = sessionFactory.openSession();
        this.session.getTransaction().begin();
    }

    public void tearDown() {
        session.getTransaction().rollback();
        session.close();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                persistenceContextProperties.getProperty("hibernate.connection.url"),
                persistenceContextProperties.getProperty("hibernate.connection.username"),
                persistenceContextProperties.getProperty("hibernate.connection.password"));
    }


    public Session getHibernateSession() {
        if (this.session == null){
            this.session = this.sessionFactory.openSession();
        }
        return this.session;
    }
}
