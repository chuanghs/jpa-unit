package org.ormunit;

import org.ormunit.entity.EntityAccessor;
import org.ormunit.entity.FieldAccessor;
import org.ormunit.entity.PropertyAccessor;

import java.lang.ref.WeakReference;
import java.util.Properties;
import java.util.WeakHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 10.02.11
 * Time: 20:49
 */
public abstract class AORMProvider implements ORMProvider {

    private final WeakHashMap<Class, WeakReference<EntityAccessor>> inspectors = new WeakHashMap<Class, WeakReference<EntityAccessor>>();

    protected Properties flatten(Properties persistenceContextProperties) {
        Properties result = new Properties();
        for (String s : persistenceContextProperties.stringPropertyNames()) {
            result.setProperty(s, persistenceContextProperties.getProperty(s));
        }
        return result;
    }

    protected Object getDefault(Class<?> idType) {
        if (boolean.class.equals(idType))
            return false;
        else if (int.class.equals(idType))
            return 0;
        else if (long.class.equals(idType))
            return 0l;
        else if (byte.class.equals(idType))
            return (byte) 0;
        else if (float.class.equals(idType))
            return 0f;
        else if (double.class.equals(idType))
            return 0d;
        else if (char.class.equals(idType))
            return (char) 0;

        return null;
    }

    public EntityAccessor getAccessor(Class<?> clazz, Class<?> defaultAcessClass) {
        if (inspectors.get(clazz) == null) {

            if (isPropertyAccessed(clazz)) {
                inspectors.put(clazz, new WeakReference<EntityAccessor>(new PropertyAccessor(clazz)));
            } else if (isFieldAccessed(clazz)) {
                inspectors.put(clazz, new WeakReference<EntityAccessor>(new FieldAccessor(clazz)));
            } else {
                if (defaultAcessClass == null)
                    throw new RuntimeException("invalid entity class, its neither PropertyAccessed nor FieldAccessed entity");
                else {
                    if (isPropertyAccessed(defaultAcessClass)) {
                        inspectors.put(clazz, new WeakReference<EntityAccessor>(new PropertyAccessor(clazz)));
                    } else if (isFieldAccessed(defaultAcessClass)) {
                        inspectors.put(clazz, new WeakReference<EntityAccessor>(new FieldAccessor(clazz)));
                    } else {
                        throw new RuntimeException("invalid entity class, its neither PropertyAccessed nor FieldAccessed entity");
                    }
                }

            }
        }
        return inspectors.get(clazz).get();
    }


    public abstract boolean isFieldAccessed(Class<?> clazz);

    public abstract boolean isPropertyAccessed(Class<?> clazz);
}
