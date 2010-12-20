package org.jpaunit.junit;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 20.12.10
 * Time: 21:36
 */
public abstract class JPAUnitTestCase {

    private String persistenceUnitName;

    public JPAUnitTestCase(String persistenceUnitName){
        this.persistenceUnitName = persistenceUnitName;

    }

}
