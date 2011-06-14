package org.ormunit;

import org.ormunit.junit.JPAHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.ormunit.ORMUnitHelper.isHSQL;
import static org.ormunit.junit.JPAHelper.*;


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
    private BeanUtils utils = new BeanUtils();


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
            getEntityManager().flush();

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

    public boolean isFieldAccessed(Class<?> clazz) {
        while (clazz != null) {
            for (Field m : clazz.getDeclaredFields()) {
                if (m.getAnnotation(Id.class) != null || m.getAnnotation(EmbeddedId.class) != null)
                    return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }


    public <T> T getEntity(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    private boolean isIdGenerated(Object entity) throws IntrospectionException {
        if (isPropertyAccessed(entity.getClass())) {
            for (Field field : utils.getFieldsAnnotatedWith(entity.getClass(), Id.class)) {
                if (field.getAnnotation(GeneratedValue.class) != null) {
                    return true;
                }
            }
        } else if (isFieldAccessed(entity.getClass())) {
            for (PropertyDescriptor pd : utils.getPropertiesAnnotatedWith(entity.getClass(), Id.class)) {
                if (pd.getReadMethod().getAnnotation(GeneratedValue.class) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public Object getId(Object entity) throws Exception {
        IdClass idClass = entity.getClass().getAnnotation(IdClass.class);
        Object result = null;
        if (idClass != null) {
            result = idClass.value().newInstance();
            if (isPropertyAccessed(entity.getClass())) {
                result = idClass.value().newInstance();
                utils.copyPropertyValues(entity, result);
            } else {
                result = idClass.value().newInstance();
                utils.copyFieldValues(entity, result);
            }
        } else {
            if (isPropertyAccessed(entity.getClass())) {
                Set<PropertyDescriptor> idProperties = utils.getPropertiesAnnotatedWith(entity.getClass(), Id.class, EmbeddedId.class);
                result = idProperties.iterator().next().getReadMethod().invoke(entity);
            } else {
                Set<Field> idFields = utils.getFieldsAnnotatedWith(entity.getClass(), Id.class, EmbeddedId.class);
                Field next = idFields.iterator().next();
                next.setAccessible(true);
                result = next.get(entity);
            }
        }
        return result;
    }

    public void setId(Object entity, Object id) throws Exception {
        if (entity.getClass().getAnnotation(IdClass.class) != null) {
            if (isPropertyAccessed(entity.getClass()))
                utils.copyPropertyValues(id, entity);
            else if (isFieldAccessed(entity.getClass()))
                utils.copyFieldValues(id, entity);
        } else {
            if (isPropertyAccessed(entity.getClass())) {
                utils.getPropertiesAnnotatedWith(entity.getClass(), Id.class, EmbeddedId.class).iterator().next().getWriteMethod().invoke(entity, id);
            } else if (isFieldAccessed(entity.getClass())) {
                Field next = utils.getFieldsAnnotatedWith(entity.getClass(), Id.class, EmbeddedId.class).iterator().next();
                next.setAccessible(true);
                next.set(entity, id);
            }
        }

    }

    public Class<?> getIdType(Class<?> entityClass) {
        Class<?> result = null;
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            result = idClass.value();
        } else {
            if (isPropertyAccessed(entityClass)) {
                return utils.getPropertiesAnnotatedWith(entityClass, Id.class, EmbeddedId.class).iterator().next().getPropertyType();
            } else if (isFieldAccessed(entityClass)) {
                return utils.getFieldsAnnotatedWith(entityClass, Id.class, EmbeddedId.class).iterator().next().getType();
            }
        }
        return result;
    }


    public Connection getConnection() throws SQLException {
        if (PersistenceProviderEclipseLink.equals(persistenceProvider)) {
            return DriverManager.getConnection(
                    persistenceContextProperties.getProperty("javax.persistence.jdbc.url"),
                    persistenceContextProperties.getProperty("javax.persistence.jdbc.user"),
                    persistenceContextProperties.getProperty("javax.persistence.jdbc.password"));
        } else if (PersistenceProviderOpenJPA.equals(persistenceProvider)) {
            return DriverManager.getConnection(
                    persistenceContextProperties.getProperty("openjpa.ConnectionURL"),
                    persistenceContextProperties.getProperty("openjpa.ConnectionUserName"),
                    persistenceContextProperties.getProperty("openjpa.ConnectionPassword"));
        } else if (PersistenceProviderHibernate.equals(persistenceProvider)) {
            return DriverManager.getConnection(
                    persistenceContextProperties.getProperty("hibernate.connection.url"),
                    persistenceContextProperties.getProperty("hibernate.connection.username"),
                    persistenceContextProperties.getProperty("hibernate.connection.password"));
        }
        throw new RuntimeException("unknown Persistence Provider");
    }

    public Set<Class<?>> getManagedTypes() {
        if (this.unitName !=null)
            return JPAHelper.getManagedTypes(getClass(), this.unitName);
        return new HashSet<Class<?>>();
    }

    private Connection con = null;

    public void setUp() {
        if (selfManagedEM) {
            createSchemas();
            // creating entitymanagerfactory for given connection properties and persistence unit name
            entityManagerFactory = Persistence.createEntityManagerFactory(
                    unitName,
                    flatten(persistenceContextProperties));
            entityManager = entityManagerFactory.createEntityManager();
        }

        entityManager.getTransaction().begin();

    }

    private void createSchemas() {
        try {
            Dialect dialect = getDialect();
            con = getConnection();
            if (con != null) {
                for (Class<?> c : getManagedTypes()) {
                    try {
                        String x = extractSchemaName(c);
                        if (x != null) {
                            con.prepareStatement(dialect.getCreateSchemaStatement(x)).executeUpdate();
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
    }


    public void tearDown() {
        if (entityManager != null) {
            entityManager.clear();
            entityManager.getTransaction().rollback();
            if (selfManagedEM) {
                entityManager.close();
                entityManagerFactory.close();
            }
        } else {
            log.warn("teardown is unnecessary because entityManager is null. Are there errors while creating EnittyManager?");
        }
        try {
            con.close();
        } catch (SQLException e) {
            log.error("", e);
        }
    }

    public Dialect getDialect() {
        if (isHSQL()) {
            return new HSQLDialect();
        } else {
            return new DefaultDialect();
        }
    }
}
