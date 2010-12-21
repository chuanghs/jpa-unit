package org.ormunit.command;

import org.ormunit.ORMProvider;
import org.ormunit.ORMUnitIntrospector;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:43
 * To change this template use File | Settings | File Templates.
 */
public class JPAORMProvider implements ORMProvider {


    private EntityManager entityManager;

    public JPAORMProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void entity(Object entity) {
        getEntityManager().persist(entity);
    }

    public void statement(String statement) {
        getEntityManager().createNativeQuery(statement).executeUpdate();
    }

    public <T> T getReference(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Class<?> getIdType(Class<?> propertyType) {
        Class type = propertyType;
        do {
            for (Field f : type.getDeclaredFields()) {
                if (f.getAnnotation(Id.class) != null)
                    return f.getType();
            }
        } while ((type = type.getSuperclass()) != null);


        try {
            for (PropertyDescriptor pd : ORMUnitIntrospector.getInspector(propertyType).getPDS()) {
                if (pd.getReadMethod() != null) {
                    if (pd.getReadMethod().getAnnotation(Id.class) != null) {
                        return pd.getPropertyType();
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
