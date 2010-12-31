package org.ormunit;

import org.ormunit.entity.EntityAccessor;
import org.ormunit.entity.FieldAccessor;
import org.ormunit.entity.PropertyAccessor;

import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:43
 */
public class JPAORMProvider implements ORMProvider {


    private EntityManager entityManager;
    private final WeakHashMap<Class, WeakReference<EntityAccessor>> inspectors = new WeakHashMap<Class, WeakReference<EntityAccessor>>();

    public EntityAccessor getAccessor(Class<?> clazz) {
        if (inspectors.get(clazz) == null) {

            if (isPropertyAccessed(clazz)) {
                inspectors.put(clazz, new WeakReference<EntityAccessor>(new PropertyAccessor(clazz)));
            } else if (isFieldAccessed(clazz)) {
                inspectors.put(clazz, new WeakReference<EntityAccessor>(new FieldAccessor(clazz)));
            } else
                throw new RuntimeException("invalid entity class, its neither PropertyAccessed nor FieldAccessed entity");
        }
        return inspectors.get(clazz).get();
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
                if (m.getAnnotation(Id.class) != null)
                    return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public JPAORMProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void entity(Object entity) {
        getEntityManager().merge(entity);
        getEntityManager().flush();
        getEntityManager().clear();
    }

    public void statement(String statement) {
        getEntityManager().createNativeQuery(statement).executeUpdate();
        getEntityManager().flush();
        getEntityManager().clear();
    }

    public <T> T getReference(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class getCollectionParameterType(Class<?> entityClass, String propertyName) {
        // TODO: extract field or property parameter type
        return Object.class;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    private WeakHashMap<Class, WeakReference<Class>> idTypes = new WeakHashMap<Class, WeakReference<Class>>();

    public Class<?> getIdType(Class<?> entityClass) {
        Class<?> result = null;
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass != null) {
            result = idClass.value();
        }
        do {
            for (Field f : entityClass.getDeclaredFields()) {
                f.setAccessible(true);
                EmbeddedId embeddedId = f.getAnnotation(EmbeddedId.class);
                if (embeddedId != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f.getType();
                    continue;
                }

                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f.getType();
                }
            }

            for (Method f : entityClass.getDeclaredMethods()) {
                f.setAccessible(true);
                EmbeddedId embeddedId = f.getAnnotation(EmbeddedId.class);
                if (embeddedId != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f.getReturnType();
                    continue;
                }

                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    if (result != null)
                        throw new RuntimeException("Invalid entity: ambiguous identifier");
                    result = f.getReturnType();
                }
            }
            entityClass = entityClass.getSuperclass();
        } while (entityClass != null && result == null);
        return result;
    }


}
