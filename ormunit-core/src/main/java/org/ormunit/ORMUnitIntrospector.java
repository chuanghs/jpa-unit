package org.ormunit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 21.12.10
 * Time: 21:15
 */
public class ORMUnitIntrospector {

    private static final Logger log = LoggerFactory.getLogger(ORMUnitIntrospector.class);
    private static final WeakHashMap<Class, WeakReference<ORMUnitIntrospector>> inspectors = new WeakHashMap<Class, WeakReference<ORMUnitIntrospector>>();
    private static final Set<Class> simpleTypes = new HashSet<Class>(Arrays.asList(
            Integer.class, int.class,
            Double.class, double.class,
            Boolean.class, boolean.class,
            Long.class, long.class,
            Float.class, float.class,
            Date.class,
            Timestamp.class,
            String.class));


    public static ORMUnitIntrospector getInspector(Class<?> clazz) throws IntrospectionException {
        if (inspectors.get(clazz) == null) {
            inspectors.put(clazz, new WeakReference<ORMUnitIntrospector>(new ORMUnitIntrospector(clazz)));
        }
        return inspectors.get(clazz).get();
    }


    private Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();
    private Class<?> clazz;

    public ORMUnitIntrospector(Class<?> clazz) throws IntrospectionException {
        this.clazz = clazz;
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        for (PropertyDescriptor pd : propertyDescriptors) {
            descriptors.put(pd.getName(), pd);
        }
    }

    public PropertyDescriptor getPD(String propertyName) {
        return descriptors.get(propertyName);
    }

    public Class<?> getPropertyType(String propertyName) {
        PropertyDescriptor propertyDescriptor = descriptors.get(propertyName);
        if (propertyDescriptor!=null)
            return propertyDescriptor.getPropertyType();
        else   {
            log.warn("no property: "+propertyName+" in class: "+clazz.getCanonicalName());
            return null;
        }
    }


    public boolean isSimpleType(String propertyName) {
        return isSimpleType(descriptors.get(propertyName).getPropertyType());
    }

    public boolean isSimpleType(Class<?> propertyType) {
        return simpleTypes.contains(propertyType);
    }

    public Object newInstance(String nodeName) throws IllegalAccessException, InstantiationException {
        return getPD(nodeName).getPropertyType().newInstance();
    }

    public void set(Object entity, String propertyName, Object value) throws InvocationTargetException, IllegalAccessException {
        PropertyDescriptor pd = getPD(propertyName);
        if (pd == null) {
            log.warn("attribute: " + pd.getName() + " does not have corresponding property in class: " + clazz.getCanonicalName());
            return;
        }
        Method setter = pd.getWriteMethod();
        if (setter == null) {
            log.warn("there is no setter for property: " + pd.getName() + " of class: " + clazz.getCanonicalName());
            return;
        }

        setter.invoke(entity, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ORMUnitIntrospector that = (ORMUnitIntrospector) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return clazz != null ? clazz.hashCode() : 0;
    }

    public Collection<PropertyDescriptor> getPDS() {
        return Collections.unmodifiableCollection(descriptors.values());
    }
}
