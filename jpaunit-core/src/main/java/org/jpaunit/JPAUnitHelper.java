package org.jpaunit;

import javax.persistence.Id;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 22:13
 */
public class JPAUnitHelper {
    public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    public static DateFormat tf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static Class<?> getIdType(Class<?> propertyType) {
        Class type = propertyType;
        do {
            for (Field f : type.getDeclaredFields()) {
                if (f.getAnnotation(Id.class) != null)
                    return f.getType();
            }
        } while ((type = type.getSuperclass()) != null);


        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(propertyType).getPropertyDescriptors()) {
                if (pd.getReadMethod() != null) {
                    if (pd.getReadMethod().getAnnotation(Id.class) != null) {
                        return pd.getPropertyType();
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static Object convert(Class<?> propertyType, String value) throws ParseException {
        if (propertyType.equals(Integer.class) || propertyType.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (propertyType.equals(Double.class) || propertyType.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (propertyType.equals(Boolean.class) || propertyType.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (propertyType.equals(Long.class) || propertyType.equals(long.class)) {
            return Long.parseLong(value);
        } else if (propertyType.equals(Float.class) || propertyType.equals(float.class)) {
            return Float.parseFloat(value);
        } else if (propertyType.equals(Date.class)) {
            return df.parse(value);
        } else if (propertyType.equals(Timestamp.class)) {
            return new Timestamp(tf.parse(value).getTime());
        } else if (propertyType.equals(String.class)) {
            return value;
        }
        throw new RuntimeException("unsupported propertyType: " + propertyType.getCanonicalName());
    }
}
