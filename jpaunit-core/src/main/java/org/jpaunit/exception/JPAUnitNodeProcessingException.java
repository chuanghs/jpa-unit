package org.jpaunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public class JPAUnitNodeProcessingException extends Exception {
    public JPAUnitNodeProcessingException() {
    }

    public JPAUnitNodeProcessingException(String message) {
        super(message);
    }

    public JPAUnitNodeProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public JPAUnitNodeProcessingException(Throwable cause) {
        super(cause);
    }
}
