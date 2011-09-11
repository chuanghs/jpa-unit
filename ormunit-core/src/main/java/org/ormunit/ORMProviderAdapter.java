package org.ormunit;

import org.ormunit.node.entity.accessor.AEntityAccessor;
import org.ormunit.node.entity.accessor.EntityAccessor;
import org.ormunit.exception.AccessorException;
import org.ormunit.exception.EntityAccessException;
import org.ormunit.exception.UnknownAccessTypeException;
import org.ormunit.node.entity.accessor.FieldAccessor;
import org.ormunit.node.entity.accessor.PropertyAccessor;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 10.02.11
 * Time: 20:49
 */
public abstract class ORMProviderAdapter implements ORMProvider {

    public static enum AccessType {
        Field, Property
    }

    private final WeakHashMap<Class, WeakReference<EntityAccessor>> accessors = new WeakHashMap<Class, WeakReference<EntityAccessor>>();

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

    public EntityAccessor getAccessor(Class<?> clazz, Class<?> inheritAcessTypeFromThisClass) {
        if (accessors.get(clazz) == null) {
            AEntityAccessor accessor = null;
            try {
                accessor = getEntityAccessor(clazz);
            } catch (UnknownAccessTypeException e) {
                if (inheritAcessTypeFromThisClass == null)
                    throw new EntityAccessException(String.format("Could not inferr access type of class %s.", clazz.getCanonicalName()), e);
                else {
                    try {
                        accessor = createAccessor(clazz, getAccessType(inheritAcessTypeFromThisClass));
                    } catch (UnknownAccessTypeException e1) {
                        throw new AccessorException(
                                String.format("Could not inferr proper access type neither from given class %s nor from \"inheritAcessTypeFromThisClass\" argument: %s",
                                        clazz.getCanonicalName(),
                                        inheritAcessTypeFromThisClass.getCanonicalName()), e);
                    }
                }
            }
            accessors.put(clazz, new WeakReference<EntityAccessor>(accessor));
        }
        return accessors.get(clazz).get();
    }

    /**
     * @param clazz
     * @return
     * @throws UnknownAccessTypeException if no access type could be inferred from given class argumet
     */
    private AEntityAccessor getEntityAccessor(Class<?> clazz) throws UnknownAccessTypeException {
        return createAccessor(clazz, getAccessType(clazz));
    }

    /**
     * @param clazz
     * @param accessType
     * @return
     * @throws IllegalStateException if given access type is neither Field nor Property
     */
    private AEntityAccessor createAccessor(Class<?> clazz, AccessType accessType) {
        if (accessType == AccessType.Field) {
            return new FieldAccessor(clazz);
        } else if (accessType == AccessType.Property) {
            return new PropertyAccessor(clazz);
        }
        throw new IllegalStateException(String.format("Requested access type for clazz %s is null ", clazz.getCanonicalName()));
    }

    public abstract AccessType getAccessType(Class<?> clazz) throws UnknownAccessTypeException;

}
