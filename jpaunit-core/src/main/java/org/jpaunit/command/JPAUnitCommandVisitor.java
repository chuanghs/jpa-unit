package org.jpaunit.command;

import javax.persistence.EntityManager;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class JPAUnitCommandVisitor {

    private EntityManager entityManager;

    public JPAUnitCommandVisitor(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    public void entity(Object entity){
        this.entityManager.persist(entity);
    }

    public void statement(String statement){
        this.entityManager.createNativeQuery(statement).executeUpdate();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
