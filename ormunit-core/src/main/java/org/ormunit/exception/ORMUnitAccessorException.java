package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 01.01.11
 * Time: 00:02
 */
public class ORMUnitAccessorException extends RuntimeException {
    public ORMUnitAccessorException(String message) {
        super(message);
    }

    public ORMUnitAccessorException(Throwable e) {
        super(e);
    }
}
