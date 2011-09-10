package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 01.01.11
 * Time: 00:02
 */
public class AccessorException extends RuntimeException {
    public AccessorException(String message) {
        super(message);
    }

    public AccessorException(Exception e) {
        super(e);
    }

    public AccessorException(String message, Exception e) {
        super(message, e);
    }
}
