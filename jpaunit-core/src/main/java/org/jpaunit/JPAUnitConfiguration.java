package org.jpaunit;

import javax.persistence.EntityManager;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 20:56
 */
public class JPAUnitConfiguration {

     private List<String> statements = new LinkedList<String>();

     public void addStatement(String nodeValue) {
        statements.add(nodeValue);
    }

    public void addStatement(Integer index, String nodeValue) {
        statements.add(index, nodeValue);
    }

    public void executeStatements(EntityManager entityManager) {
        for (String s : statements){
            entityManager.createNativeQuery(s).executeUpdate();
        }
    }

}
