package org.ormunit.command;

import javax.persistence.EntityManager;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:03
 */
public class JPAUnitCommandVisitor implements ORMCommandVisitor {

    private EntityManager entityManager;

    public JPAUnitCommandVisitor(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    public void entity(Object entity){
        getEntityManager().persist(entity);
    }

    public void statement(String statement){
        getEntityManager().createNativeQuery(statement).executeUpdate();
    }

    public <T> T getReference(Class<T> propertyClass, Object id) {
        return getEntityManager().getReference(propertyClass, id);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
