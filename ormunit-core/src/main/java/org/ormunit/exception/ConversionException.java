package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 23.12.10
 * Time: 14:01
 */
public class ConversionException extends RuntimeException {

    public ConversionException(Throwable pe) {
        super(pe);
    }

    public ConversionException(String pe) {
        super(pe);
    }

}
