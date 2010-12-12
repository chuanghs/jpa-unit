package org.jpaunit.exception;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 20:47
 */
public class JPAUnitFileReadException extends Throwable {

    public JPAUnitFileReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public JPAUnitFileReadException() {
    }

    public JPAUnitFileReadException(String message) {
        super(message);
    }

    public JPAUnitFileReadException(Exception e) {

    }
}
