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

    private Map<String, Field> fields = new HashMap<String, Field>();
    private Class<?> clazz;

    public FieldAccessor(Class<?> clazz) {
        this.clazz = clazz;
        for (Field f : clazz.getDeclaredFields()) {
            fields.put(f.getName(), f);
        }
    }

    public Field[] getFields() {
        return fields.values().toArray(new Field[fields.size()]);
    }

    public Class getType(String propertyName) {
        Field f = fields.get(propertyName);
        if (f != null)
            return f.getType();
        else {
            log.warn("no property: " + propertyName + " in class: " + clazz.getCanonicalName());
            return null;
        }
    }

    public void set(Object entity, String propertyName, Object value) {
        try {
            Field pd = fields.get(propertyName);
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
