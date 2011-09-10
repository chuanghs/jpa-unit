package org.ormunit;

import org.ormunit.dialect.Dialect;
import org.ormunit.exception.UnknownAccessTypeException;
import org.ormunit.jpa.entityinspector.EntityInspector;
import org.ormunit.jpa.providerproperties.ProviderProperties;
import org.ormunit.jpa.unit.FakePersistenceUnit;
import org.ormunit.jpa.unit.PersistenceUnit;
import org.ormunit.jpa.unit.XmlPersistenceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:43
 */
public class JPAORMProvider extends ORMProviderAdapter {


    private static final Logger log = LoggerFactory.getLogger(JPAORMProvider.class);

    private boolean selfManagedEM = true;
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private BeanUtils utils = new BeanUtils();
    private EntityInspector entityClassInspector;
    private PersistenceUnit persistenceUnit;
    private Properties defaultDataSourceProperties;


    public JPAORMProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.selfManagedEM = false;
        this.persistenceUnit = new FakePersistenceUnit();
        this.entityClassInspector = persistenceUnit.createClassInspector();
    }


    public JPAORMProvider(ORMUnitPropertiesReader ormUnit, String unitName) {
        this.persistenceUnit = new XmlPersistenceUnit(ormUnit.getWorkClass(), unitName);
        this.defaultDataSourceProperties = ormUnit.getDefaultDataSourceProperties(new Properties());
        this.entityClassInspector = persistenceUnit.createClassInspector();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void statement(String statement) {
        getEntityManager().createNativeQuery(statement).executeUpdate();
    }

    public boolean isEmbeddable(Class propertyType) {
        return propertyType.getAnnotation(Embeddable.class) != null;
    }

    public Object entity(Object entity) {
        try {
            if (entityClassInspector.isIdGenerated(entity.getClass())) {
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
        return entityClassInspector.getAccessTypeOfClass(clazz) == AccessType.Field;
    }

    public boolean isPropertyAccessed(Class clazz) {
        return entityClassInspector.getAccessTypeOfClass(clazz) == AccessType.Property;
    }

    public <T> T getEntity(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);
    }

    public Object getId(Object entity) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> entityClass = entity.getClass();
        Class idClassType = entityClassInspector.getIdClassValue(entityClass);
        Object result = null;
        if (idClassType != null) {
            result = idClassType.newInstance();
            if (entityClassInspector.getAccessTypeOfClass(entityClass) == AccessType.Property) {
                result = utils.copyPropertyValues(entity, idClassType.newInstance());
            } else {
                result = utils.copyFieldValues(entity, idClassType.newInstance());
            }
        } else {
            if (entityClassInspector.getAccessTypeOfClass(entityClass) == AccessType.Property) {
                result = entityClassInspector.getIdProperty(entityClass).getReadMethod().invoke(entity);
            } else {
                Field next = entityClassInspector.getIdField(entityClass);
                next.setAccessible(true);
                result = next.get(entity);
            }
        }
        return result;
    }

    public void setId(Object entity, Object id) throws IllegalAccessException, InvocationTargetException {
        Class<?> entityClass = entity.getClass();
        Class idClassType = entityClassInspector.getIdClassValue(entityClass);
        if (idClassType != null) {
            if (entityClassInspector.getAccessTypeOfClass(entityClass) == AccessType.Property)
                utils.copyPropertyValues(id, entity);
            else if (entityClassInspector.getAccessTypeOfClass(entityClass) == AccessType.Field)
                utils.copyFieldValues(id, entity);
        } else {
            if (entityClassInspector.getAccessTypeOfClass(entityClass) == AccessType.Property) {
                entityClassInspector.getIdProperty(entityClass).getWriteMethod().invoke(entity, id);
            } else if (entityClassInspector.getAccessTypeOfClass(entityClass) == AccessType.Field) {
                Field next = entityClassInspector.getIdField(entityClass);
                next.setAccessible(true);
                next.set(entity, id);
            }
        }
    }

    public Class<?> getIdType(Class<?> entityClass) {
        return entityClassInspector.getIdType(entityClass);
    }

    public Set<Class<?>> getManagedTypes() {
        return persistenceUnit.getManagedTypes();
    }

    private Connection con = null;

    public void setUp() {
        if (selfManagedEM) {
            ProviderProperties providerProperties = persistenceUnit.createProviderProperties(defaultDataSourceProperties);

            createSchemas(providerProperties);

            entityManagerFactory = Persistence.createEntityManagerFactory(
                    persistenceUnit.getUnitName(),
                    providerProperties.getEntityManagerFactoryProperties());
            entityManager = entityManagerFactory.createEntityManager();
        }

        entityManager.getTransaction().begin();


    }

    private void createSchemas(ProviderProperties providerProperties) {
        try {
            Dialect dialect = providerProperties.getDialect();
            con = providerProperties.openConnection();
            if (con != null) {
                for (Class<?> c : getManagedTypes()) {
                    try {
                        String x = entityClassInspector.getSchemaName(c);
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

        if (selfManagedEM)
            try {
                con.close();
            } catch (SQLException e) {
                log.error("", e);
            }
    }

    @Override
    public AccessType getAccessType(Class<?> clazz) throws UnknownAccessTypeException {
        if (isFieldAccessed(clazz)) {
            return AccessType.Field;
        } else if (isPropertyAccessed(clazz)) {
            return AccessType.Property;
        }
        throw new UnknownAccessTypeException(String.format("Class %s is neither Field nor Property accessed. Did you forget to use %s or %s annotation.",
                clazz.getCanonicalName(),
                Id.class.getCanonicalName(),
                EmbeddedId.class.getCanonicalName()));
    }
}
