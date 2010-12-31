package org.ormunit.entity;

import org.ormunit.exception.ORMEntityAccessException;
import org.ormunit.exception.ORMUnitInstantiationException;

import java.lang.reflect.*;
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


    protected Class extractClass(Type type) {
        if (type instanceof Class)
            return (Class) type;
        if (type instanceof WildcardType) {
            if (((WildcardType) type).getLowerBounds().length > 0) {
                return extractClass(((WildcardType) type).getLowerBounds()[0]);
            } else if (((WildcardType) type).getUpperBounds().length > 0) {
                return extractClass(((WildcardType) type).getUpperBounds()[0]);
            }
        } else if (type instanceof ParameterizedType) {
            return extractClass(((ParameterizedType) type).getActualTypeArguments()[0]);
        } else if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type;

            Class c = getEntityClass();
            do {
                Type genericSuperclass = c.getGenericSuperclass();
                int index = 0;
                if (genericSuperclass != null) {
                    for (TypeVariable dtv : c.getSuperclass().getTypeParameters()) {
                        if (dtv.equals(tv)) {
                            return extractClass(((ParameterizedType) genericSuperclass).getActualTypeArguments()[index]);
                        }
                        index++;
                    }
                }
                c = c.getSuperclass();
            } while (c != null);


            if (tv.getBounds().length > 0)
                return extractClass(tv.getBounds()[0]);
        }

        return null;  //To change body of created methods use File | Settings | File Templates.
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
