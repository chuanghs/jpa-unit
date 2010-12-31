package org.ormunit.entity;

import org.ormunit.exception.ORMEntityAccessException;
import org.ormunit.exception.ORMUnitInstantiationException;

import java.lang.reflect.Modifier;
import java.util.*;

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

        Class<?> type = getType(propertyName);
        if (type == null)
            throw new ORMUnitInstantiationException("no such property: " + propertyName + " in class: " + getEntityClass().getCanonicalName());

        if (Collection.class.isAssignableFrom(type)) {
            if (!type.isInterface() && Modifier.isAbstract(type.getModifiers())) {
                throw new ORMUnitInstantiationException("cannot instantiate abstract collections, property: " + propertyName + " in class " + getEntityClass().getCanonicalName());
            } else {
                if (List.class.equals(type)) {
                    return new LinkedList();
                } else if (Set.class.equals(type)) {
                    return new HashSet();
                } else if (Collection.class.equals(type)) {
                    return new LinkedList();
                }
            }
        } else if (!isSimpleType(type)) {
            try {
                return type.newInstance();
            } catch (Exception e) {
                throw new ORMUnitInstantiationException(e);
            }
        } else {
            throw new ORMEntityAccessException("cannot create simple types, property: " + propertyName + " in class " + getEntityClass().getCanonicalName());
        }
        throw new ORMUnitInstantiationException("cannon instantiate object of type: " + type.getCanonicalName() + ", property: " + propertyName + " in class " + getEntityClass().getCanonicalName());
    }


}
