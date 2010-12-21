package org.ormunit;

import org.ormunit.exception.ORMUnitConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 22:13
 */
public class ORMUnitHelper {

    public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    public static DateFormat tf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


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

    public static Properties readOrmUnitProperties(Class<?> start) {

        String[] split = start.getPackage().getName().split("\\.");
        int i = 0;
        String path = "/";
        Properties defaults = readDefaults();
        Properties result = new Properties();
        try {
            do {
                InputStream propertiesStream = start.getResourceAsStream(path + ORMUnitConfigurationReader.JPAUnitPropertiesFileName);
                result = new Properties();
                result.putAll(defaults);
                if (propertiesStream != null)
                    result.load(propertiesStream);
                defaults = result;
                if (i < split.length)
                    path = path + split[i] + "/";
                i++;
            } while (i <= split.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static Properties readDefaults() {
        InputStream resourceAsStream = ORMUnitConfiguration.class.getResourceAsStream("/"+ORMUnitConfigurationReader.JPAUnitDefaultPropertiesFileName);
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new ORMUnitConfigurationException(e);
        } finally {
            if (resourceAsStream != null)
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    // dont care about that
                }
        }
        return properties;
    }
}