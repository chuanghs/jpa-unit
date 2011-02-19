package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 20:47
 */
public class ORMUnitFileReadException extends Exception {

    public ORMUnitFileReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ORMUnitFileReadException(String message) {
        super(message);
    }

    public ORMUnitFileReadException(Throwable e) {
    }
}
