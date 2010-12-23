package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 23.12.10
 * Time: 14:01
 */
public class ConvertionException extends Throwable {

    public ConvertionException(Throwable pe) {
        super(pe);
    }

    public ConvertionException(String pe) {
        super(pe);
    }

}
