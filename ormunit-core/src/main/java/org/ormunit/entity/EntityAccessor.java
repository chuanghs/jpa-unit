package org.ormunit.entity;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 15:35
 */
public interface EntityAccessor {

    Set<Class> simpleTypes = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(
            Byte.class, byte.class,
            Character.class, char.class,
            Integer.class, int.class,
            Double.class, double.class,
            Boolean.class, boolean.class,
            Long.class, long.class,
            Float.class, float.class,
            Date.class,
            Timestamp.class,
            String.class)));

    Class getEntityClass();

    Object newInstance(String propertyName);

    Class getType(String propertyName);

    void set(Object entity, String propertyName, Object value);

    Object get(Object entity, String propertyName);

    Class getCollectionParameterType(String propertyName);

    Class[] getMapParameterTypes(String propertyName);
}
