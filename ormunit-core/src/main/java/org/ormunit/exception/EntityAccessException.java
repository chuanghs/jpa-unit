package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 15:40
 */
public class EntityAccessException extends RuntimeException {
    public EntityAccessException(Throwable cause) {
        super(cause);
    }

    public EntityAccessException(String cause) {
        super(cause);
    }

    public EntityAccessException(String message, UnknownAccessTypeException cause) {
        super(message, cause);
    }
}
