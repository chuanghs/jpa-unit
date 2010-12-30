package org.ormunit;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
public interface ORMProvider {

    Class<?> getIdType(Class<?> propertyType);

}
