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


    protected Class getCollectionParameterType(String propertyName, ParameterizedType genericType) {
        return extractClass(getTypeValue(genericType, Collection.class, 0));
    }

    protected Class[] getMapParameterTypes(String propertyName, ParameterizedType genericType) {
        return new Class[]{extractClass(getTypeValue(genericType, Map.class, 0)), extractClass(getTypeValue(genericType, Map.class, 1))};
    }

    private Type getTypeValue(ParameterizedType genericType, Class<?> searchingFor, int index) {
        Map<TypeVariable, Type> valuesMap = new HashMap<TypeVariable, Type>();

        Class clazz = getEntityClass();
        do {
            if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
                Type[] values = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
                TypeVariable[] variables = clazz.getSuperclass().getTypeParameters();

                for (int i = 0; i < values.length; i++) {
                    valuesMap.put(variables[i], values[i]);
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);

        Type value = getTypeValue(genericType, searchingFor, index, valuesMap);

        while (valuesMap.containsKey(value)) {
            value = valuesMap.get(value);
        }

        if (value instanceof ParameterizedType) {
            value = ((ParameterizedType) value).getActualTypeArguments()[0];
        }

        return value;
    }


    private Type getTypeValue(ParameterizedType genericType, Class<?> searchingFor, int index, Map<TypeVariable, Type> valuesMap) {
        if (genericType == null)
            return null;


        Class rawType = (Class) genericType.getRawType();
        Type[] values = genericType.getActualTypeArguments();
        TypeVariable[] variables = rawType.getTypeParameters();

        for (int i = 0; i < values.length; i++) {
            valuesMap.put(variables[i], values[i]);
        }

        Type result = null;
        if (genericType.getRawType().equals(searchingFor)) {
            result = valuesMap.get(variables[index]);
        } else {

            for (Type t : rawType.getGenericInterfaces()) {
                result = getTypeValue((ParameterizedType) t, searchingFor, index, valuesMap);
                if (result != null)
                    break;
            }
            if (result == null)
                result = getTypeValue((ParameterizedType) rawType.getGenericSuperclass(), searchingFor, index, valuesMap);
        }
        return result;
    }


    private Class extractClass(Type type) {
        Class result = null;
        if (type instanceof Class)
            result = (Class) type;
        if (type instanceof WildcardType) {
            if (((WildcardType) type).getLowerBounds().length > 0) {
                result = extractClass(((WildcardType) type).getLowerBounds()[0]);
            } else if (((WildcardType) type).getUpperBounds().length > 0) {
                result = extractClass(((WildcardType) type).getUpperBounds()[0]);
            }
        } else if (type instanceof ParameterizedType) {
            result = extractClass(((ParameterizedType) type).getActualTypeArguments()[0]);
        } else if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type;

            Class c = getEntityClass();
            result = extractTypeVariableBounds(tv, c);
            if (result == null)
                result = extractTypeVariableBounds(tv, (Class) tv.getGenericDeclaration());
            if (result == null && tv.getBounds().length > 0)
                result = extractClass(tv.getBounds()[0]);

        }
        return result;
    }

    private Class extractTypeVariableBounds(TypeVariable tv, Class c) {
        if (c == null)
            return Object.class;

        Type genericSuperclass = c.getGenericSuperclass();
        if (genericSuperclass != null) {
            int index = 0;
            for (TypeVariable dtv : c.getSuperclass().getTypeParameters()) {
                if (dtv.equals(tv)) {
                    while (genericSuperclass instanceof Class) {
                        genericSuperclass = ((Class) genericSuperclass).getGenericSuperclass();
                    }
                    if (genericSuperclass != null)
                        return extractClass(((ParameterizedType) genericSuperclass).getActualTypeArguments()[index]);
                }
                index++;
            }
            c = c.getSuperclass();
            return extractTypeVariableBounds(tv, c.getSuperclass());
        }
        if (tv.getBounds().length > 0)
            return extractClass(tv.getBounds()[0]);
        return Object.class;
    }

    public Object newInstance(String propertyName) {

        Class<?> type = getType(propertyName);
        if (type == null)
            throw new ORMUnitInstantiationException("no such property: " + propertyName + " in class: " + getEntityClass().getCanonicalName());

        if (!type.isInterface() && Modifier.isAbstract(type.getModifiers())) {
            throw new ORMUnitInstantiationException("cannot instantiate abstract collections, property: " + propertyName + " in class " + getEntityClass().getCanonicalName());
        }

        if (Collection.class.isAssignableFrom(type)) {

            if (List.class.equals(type)) {
                return new LinkedList();
            } else if (Set.class.equals(type)) {
                return new HashSet();
            } else if (Collection.class.equals(type)) {
                return new LinkedList();
            }

        } else if (Map.class.isAssignableFrom(type)) {
            if (SortedMap.class.equals(type)){
                return new TreeMap();
            } else if (Map.class.equals(type)){
                return new HashMap();
            }
        } else if (!isSimpleType(type)) {
            try {
                return type.newInstance();
            } catch (Exception e) {
                throw new ORMUnitInstantiationException(e);
            }
        } else {
            throw new ORMUnitInstantiationException("cannot create simple types, property: " + propertyName + " in class " + getEntityClass().getCanonicalName());
        }
        throw new ORMUnitInstantiationException("cannon instantiate object of type: " + type.getCanonicalName() + ", property: " + propertyName + " in class " + getEntityClass().getCanonicalName());
    }


}
