package org.ormunit.entity;

import org.ormunit.exception.ORMEntityAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 21.12.10
 * Time: 21:15
 */
public class FieldAccessor extends AEntityAccessor {

    private static final Logger log = LoggerFactory.getLogger(FieldAccessor.class);

    private Map<String, Field> descriptors = new HashMap<String, Field>();
    private Class<?> clazz;

    public FieldAccessor(Class<?> clazz) {
        this.clazz = clazz;
        for (Field f : clazz.getDeclaredFields()) {
            descriptors.put(f.getName(), f);
        }
    }

    public Field[] getFields() {
        return descriptors.values().toArray(new Field[descriptors.size()]);
    }

    public Class getPropertyType(String propertyName) {
        Field f = descriptors.get(propertyName);
        if (f != null)
            return f.getType();
        else {
            log.warn("no property: " + propertyName + " in class: " + clazz.getCanonicalName());
            return null;
        }
    }


    public boolean isSimpleType(String propertyName) {
        return isSimpleType(descriptors.get(propertyName).getType());
    }


    public Object newInstance(String nodeName) {
        try {
            Class<?> type = descriptors.get(nodeName).getType();
            if (!isSimpleType(type))
                return type.newInstance();
            else {
                if (type == Integer.class || type == int.class)
                    return 0;
                if (type == Long.class || type == long.class)
                    return 0l;
                if (type == Double.class || type == double.class)
                    return 0d;
                if (type == Float.class || type == float.class)
                    return 0f;
                if (type == Boolean.class || type == boolean.class)
                    return false;
            }
        } catch (Exception e) {
            throw new ORMEntityAccessException(e);
        }
        return null;
    }

    public void set(Object entity, String propertyName, Object value) {
        try {
            Field pd = descriptors.get(propertyName);
            if (pd == null) {
                log.warn("attribute: " + pd.getName() + " does not have corresponding property in class: " + clazz.getCanonicalName());
                return;
            }
            pd.setAccessible(true);
            pd.set(entity, value);
        } catch (Exception e) {
            throw new ORMEntityAccessException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldAccessor that = (FieldAccessor) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return clazz != null ? clazz.hashCode() : 0;
    }
}
