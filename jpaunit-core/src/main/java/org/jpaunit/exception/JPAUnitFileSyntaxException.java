package org.jpaunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 21:11
 */
public class JPAUnitFileSyntaxException extends JPAUnitFileReadException {

    public JPAUnitFileSyntaxException(String message) {
        super(message);
    }

    public JPAUnitFileSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

}
