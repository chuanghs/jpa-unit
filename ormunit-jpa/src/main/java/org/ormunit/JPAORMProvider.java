package org.ormunit;

import static org.ormunit.junit.JPAHelper.*;
import static org.ormunit.ORMUnitHelper.*;

import org.ormunit.junit.JPAHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;



/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:43
 */
public class JPAORMProvider extends AORMProvider {


    private static final Logger log = LoggerFactory.getLogger(JPAORMProvider.class);

    private EntityManager entityManager;
    private String persistenceProvider;
    private String unitName;
    private Properties persistenceContextProperties;

    private boolean selfManagedEM = true;
    private EntityManagerFactory entityManagerFactory;
    private WeakHashMap<Class, WeakReference<Class>> idTypes = new WeakHashMap<Class, WeakReference<Class>>();



    public JPAORMProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.selfManagedEM = false;
    }


    public JPAORMProvider(ORMUnit ormUnit, String unitName) {
        this.unitName = unitName;

        Properties defaults = new Properties();

        persistenceProvider = getPersistenceProvider(ormUnit.getWorkClass(), unitName);
        if (persistenceProvider != null) {
            defaults = getPersistenceProviderDefaults(persistenceProvider);
        } else {
            List<String> providers = findAllProviders();
            if (providers.size() > 0) {
                persistenceProvider = providers.get(0);
                defaults = getPersistenceProviderDefaults(providers.get(0));
            }
        }
        // overriding default properties with ORMUnit.properties
        Properties ormUnitProperties = ormUnit.getDefaultDataSourceProperties(defaults);

        // overriding default properties with those defined in persistence.xml
        persistenceContextProperties = getProperties(ormUnit.getWorkClass(), unitName, ormUnitProperties);

    }

    public void statement(String statement) {
        getEntityManager().createNativeQuery(statement).executeUpdate();
    }


    public Object entity(Object entity) {

        try {
            if (isIdGenerated(entity)) {
                setId(entity, getDefault(getIdType(entity.getClass())));
            }

            getEntityManager().persist(entity);

            return getId(entity);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
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



    private boolean isIdGenerated(Object entity) throws IntrospectionException {
        Field idField = getIdField(entity.getClass());
        if (idField != null)
            return idField.getAnnotation(GeneratedValue.class) != null;

        PropertyDescriptor idProperty = getIdProperty(entity.getClass());
        if (idProperty != null)
            return idProperty.getReadMethod().getAnnotation(GeneratedValue.class) != null;

        return false;
    }


    public <T> T getEntity(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    private Field getIdField(Class<?> entityClass) {
        Field result = null;
        do {

            for (Field f : entityClass.getDeclaredFields()) {
                f.setAccessible(true);
                EmbeddedId embeddedId = f.getAnnotation(EmbeddedId.class);
                if (embeddedId != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f;
                    continue;
                }

                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f;
                }
            }
            entityClass = entityClass.getSuperclass();
        } while (entityClass != null && result == null);
        return result;
    }

    public PropertyDescriptor getIdProperty(Class<?> entityClass) throws IntrospectionException {
        PropertyDescriptor result = null;
        do {

            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(entityClass).getPropertyDescriptors();

            for (PropertyDescriptor pd : propertyDescriptors) {
                Method f = pd.getReadMethod();
                if (f == null)
                    continue;
                f.setAccessible(true);
                EmbeddedId embeddedId = f.getAnnotation(EmbeddedId.class);
                if (embeddedId != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = pd;
                    continue;
                }

                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = pd;
                }
            }
            entityClass = entityClass.getSuperclass();
        } while (entityClass != null && result == null);
        return result;
    }

    public Object getId(Object entity) throws Exception {
        Field idField = getIdField(entity.getClass());
        PropertyDescriptor idProperty = getIdProperty(entity.getClass());

        Object result = null;
        if (idField != null) {
            result = idField.get(entity);
        } else if (idProperty != null) {
            result = idProperty.getReadMethod().invoke(entity);
        }
        return result;
    }

    public void setId(Object entity, Object id) throws Exception {
        Field idField = getIdField(entity.getClass());
        PropertyDescriptor idProperty = getIdProperty(entity.getClass());

        Object result = null;
        if (idField != null) {
            idField.set(entity, id);
        } else if (idProperty != null) {
            idProperty.getWriteMethod().invoke(entity, id);
        }
    }

    public Class<?> getIdType(Class<?> entityClass) {
        Class<?> result = null;
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            result = idClass.value();
        } else {
            Field idField = getIdField(entityClass);
            PropertyDescriptor idProperty = null;
            try {
                idProperty = getIdProperty(entityClass);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }

            if (idField != null && idProperty != null)
                throw new RuntimeException("Invalid entity: ambiguous identifier");
            else if (idField != null)
                result = idField.getType();
            else
                result = idProperty.getPropertyType();
        }
        return result;
    }


    public Connection getConnection() throws SQLException {
        if (PersistenceProviderEclipseLink.equals(persistenceProvider)) {
            return DriverManager.getConnection(persistenceContextProperties.getProperty("javax.persistence.jdbc.url"), persistenceContextProperties.getProperty("javax.persistence.jdbc.user"), persistenceContextProperties.getProperty("javax.persistence.jdbc.password"));
        } else if (PersistenceProviderOpenJPA.equals(persistenceProvider)) {
            return DriverManager.getConnection(persistenceContextProperties.getProperty("openjpa.ConnectionURL"), persistenceContextProperties.getProperty("openjpa.ConnectionUserName"), persistenceContextProperties.getProperty("openjpa.ConnectionPassword"));
        } else if (PersistenceProviderHibernate.equals(persistenceProvider)) {
            return DriverManager.getConnection(persistenceContextProperties.getProperty("hibernate.connection.url"), persistenceContextProperties.getProperty("hibernate.connection.username"), persistenceContextProperties.getProperty("hibernate.connection.password"));
        }
        throw new RuntimeException("unknown Persistence Provider");
    }

    private String extractSchemaName(Class<?> c) {
        Table annotation = c.getAnnotation(Table.class);
        if (annotation != null && !"".equals(annotation.schema()))
            return annotation.schema();
        return null;
    }

    public Set<Class<?>> getManagedTypes() {
        return  JPAHelper.getManagedTypes(getClass(), this.unitName);
    }

    private Connection con = null;

    public void setUp() {
        if (selfManagedEM) {
            try {
                con = getConnection();
                if (con != null) {
                    for (Class<?> c : getManagedTypes()) {
                        try {
                            String x = extractSchemaName(c);
                            if (x != null) {
                                //log.info("creating schema: " + x);
                                if (isHSQL())
                                    con.prepareStatement("create schema " + x.toUpperCase() + " authorization DBA").executeUpdate();
                                else
                                    con.prepareStatement("create schema " + x.toUpperCase() + " authorization sa").executeUpdate();
                            }

                        } catch (Throwable e) {
                            log.error(e.getMessage());
                        }
                    }
                    con.commit();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            // creating entitymanagerfactory for given connection properties and persistence unit name
            entityManagerFactory = Persistence.createEntityManagerFactory(
                    unitName,
                    flatten(persistenceContextProperties));
            entityManager = entityManagerFactory.createEntityManager();
        }

        entityManager.getTransaction().begin();

    }


    public void tearDown() {
        entityManager.clear();
        entityManager.getTransaction().rollback();
        if (selfManagedEM) {
            entityManager.close();
            entityManagerFactory.close();
        }
        try {
            con.close();
        } catch (SQLException e) {
            log.error("", e);
        }
    }

}
