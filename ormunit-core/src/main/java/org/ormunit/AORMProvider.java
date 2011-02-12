package org.ormunit;

import org.ormunit.entity.EntityAccessor;
import org.ormunit.entity.FieldAccessor;
import org.ormunit.entity.PropertyAccessor;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 10.02.11
 * Time: 20:49
 */
public abstract class AORMProvider implements ORMProvider {

    private final WeakHashMap<Class, WeakReference<EntityAccessor>> inspectors = new WeakHashMap<Class, WeakReference<EntityAccessor>>();

     public EntityAccessor getAccessor(Class<?> clazz) {
        if (inspectors.get(clazz) == null) {

            if (isPropertyAccessed(clazz)) {
                inspectors.put(clazz, new WeakReference<EntityAccessor>(new PropertyAccessor(clazz)));
            } else if (isFieldAccessed(clazz)) {
                inspectors.put(clazz, new WeakReference<EntityAccessor>(new FieldAccessor(clazz)));
            } else
                throw new RuntimeException("invalid entity class, its neither PropertyAccessed nor FieldAccessed entity");
        }
        return inspectors.get(clazz).get();
    }

    protected boolean isFieldAccessed(Class<?> clazz){
        return false;
    }

    protected boolean isPropertyAccessed(Class<?> clazz){
        return false;
    }
}
