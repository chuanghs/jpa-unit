package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 15:40
 */
public class ORMEntityAccessException extends RuntimeException {
    public ORMEntityAccessException(Throwable cause) {
        super(cause);
    }
}
