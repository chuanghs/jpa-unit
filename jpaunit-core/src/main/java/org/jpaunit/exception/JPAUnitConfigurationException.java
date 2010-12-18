package org.jpaunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 13.12.10
 * Time: 21:05
 */
public class JPAUnitConfigurationException extends RuntimeException {

    public JPAUnitConfigurationException(String s) {
        super(s);
    }

    public JPAUnitConfigurationException(Throwable cause) {
        super(cause);
    }
}
