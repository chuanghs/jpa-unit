package org.ormunit.entity;

import org.ormunit.exception.ORMEntityAccessException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 23:44
 */
public abstract class AEntityAccessor implements EntityAccessor {

    public boolean isSimpleType(Class<?> propertyType) {
        return simpleTypes.contains(propertyType);
    }

    public boolean isSimpleType(String propertyName) {
        return isSimpleType(getType(propertyName));
    }

    public Object newInstance(String propertyName) {
        try {
            Class<?> type = getType(propertyName);
            if (!isSimpleType(type))
                return type.newInstance();
            else {
                throw new ORMEntityAccessException("cannot create simple types");
            }
        } catch (Exception e) {
            throw new ORMEntityAccessException(e);
        }
    }


}
