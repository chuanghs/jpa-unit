package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 13.12.10
 * Time: 21:05
 */
public class ORMUnitConfigurationException extends RuntimeException {

    public ORMUnitConfigurationException(String s) {
        super(s);
    }

    public ORMUnitConfigurationException(Throwable cause) {
        super(cause);
    }
}
