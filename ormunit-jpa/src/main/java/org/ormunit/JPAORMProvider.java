package org.ormunit;

import com.sun.java.xml.ns.persistence.orm.AccessType;
import org.ormunit.junit.JPAHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.ref.WeakReference;
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
    private AnnotationsClassInspector classInspector = new AnnotationsClassInspector();


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

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void statement(String statement) {
        getEntityManager().createNativeQuery(statement).executeUpdate();
    }

    public Object entity(Object entity) {
        try {
            if (classInspector.isIdGenerated(classInspector, entity, this)) {
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

    public boolean isFieldAccessed(Class<?> clazz) {
        return classInspector.getAccessType(clazz) == AccessType.FIELD;
    }

    public boolean isPropertyAccessed(Class clazz) {
        return classInspector.getAccessType(clazz) == AccessType.PROPERTY;
    }

    public <T> T getEntity(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);
    }

    public Object getId(Object entity) throws Exception {
        return classInspector.getId(entity);
    }

    public void setId(Object entity, Object id) throws Exception {
        classInspector.setId_Annotations(entity, id);

    }

    public Class<?> getIdType(Class<?> entityClass) {
        return classInspector.getIdClass2(entityClass);
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
        if (this.unitName != null)
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
