package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 21:11
 */
public class ORMUnitFileSyntaxException extends ORMUnitFileReadException {

    public ORMUnitFileSyntaxException(String message) {
        super(message);
    }

    public ORMUnitFileSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

}
