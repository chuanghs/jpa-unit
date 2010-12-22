package org.ormunit.entity;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 15:35
 */
public interface EntityAccessor {

    Set<Class> simpleTypes = new HashSet<Class>(Arrays.asList(
            Integer.class, int.class,
            Double.class, double.class,
            Boolean.class, boolean.class,
            Long.class, long.class,
            Float.class, float.class,
            Date.class,
            Timestamp.class,
            String.class));

    Object newInstance(String propertyName);

    boolean isSimpleType(String propertyName);

    Class<Object> getPropertyType(String propertyName);

    void set(Object entity, String propertyName, Object value);
}
