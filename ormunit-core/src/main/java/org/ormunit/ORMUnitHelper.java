package org.ormunit;

import org.ormunit.exception.ConfigurationException;
import org.ormunit.exception.ConversionException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static java.lang.Enum.valueOf;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 19.12.10
 * Time: 22:13
 */
public class ORMUnitHelper {

    public static final String JDBC_URL_DERBY = "jdbc:derby:memory:unit-testing-jpa;drop=true";
    public static final String JDBC_URL_HSQL = "jdbc:hsqldb:mem:unit-testing-jpa;shutdown=true";
    public static final String JDBC_URL_H2 = "jdbc:h2:mem:unit-testing-jpa";

    public static final String DerbyDriverClassName = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String H2DriverClassName = "org.h2.Driver";
    public static final String HSQLDriverClassName = "org.hsqldb.jdbcDriver";

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat tf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    public static Object convert(Class<?> targetType, String value) throws ConversionException {
        if (targetType==null)
            throw new IllegalArgumentException("targetType argument cannot be null.");
        try {
            if (targetType.equals(Object.class)) {
                return value;
            } else if (targetType.equals(BigInteger.class)) {
                return new BigInteger(value);
            }  else if (targetType.equals(BigDecimal.class)) {
                return new BigDecimal(value);
            } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return Integer.parseInt(value);
            } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                return Double.parseDouble(value);
            } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                if ("true".equalsIgnoreCase(value))
                    return true;
                if ("false".equalsIgnoreCase(value))
                    return false;
                throw new IllegalArgumentException(value + " is neither true nor false");
            } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                return Long.parseLong(value);
            } else if (targetType.equals(Short.class) || targetType.equals(short.class)) {
                return Short.parseShort(value);
            } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                return Float.parseFloat(value);
            } else if (targetType.equals(Character.class) || targetType.equals(char.class)) {
                return value.charAt(0);
            } else if (targetType.equals(Byte.class) || targetType.equals(byte.class)) {
                return Byte.parseByte(value);
            } else if (targetType.equals(Date.class)) {
                return parseDate(value);
            } else if (targetType.equals(Timestamp.class)) {
                return new Timestamp(parseTime(value).getTime());
            } else if (targetType.equals(String.class)) {
                return value;
            } else if (Enum.class.isAssignableFrom(targetType)) {
                return valueOf((Class<? extends Enum>) targetType, value);
            }
        } catch (Exception pe) {
            throw new ConversionException(pe);
        }
        throw new ConversionException("unsupported targetType: " + targetType.getCanonicalName());

    }

    private static synchronized Date parseDate(String value) throws ParseException {
        return df.parse(value);
    }

    private static synchronized Date parseTime(String value) throws ParseException {
        return tf.parse(value);
    }

    public static String getDefaultDriverClassName() {
        if (isHSQL()) {
            return HSQLDriverClassName;
        } else if (isH2()) {
            return H2DriverClassName;
        } else if (isDerby()) {
            return DerbyDriverClassName;
        }
        return null;
    }

    public static String getDefaultConnectionURL() {
        if (isHSQL()) {
            return JDBC_URL_HSQL;
        } else if (isH2()) {
            return JDBC_URL_H2;
        } else if (isDerby()) {
            return JDBC_URL_DERBY;
        }
        return null;
    }

    public static boolean isDerby() {
        return isClassAvailable(DerbyDriverClassName);
    }

    public static boolean isHSQL() {
        return isClassAvailable(HSQLDriverClassName);
    }

    public static boolean isH2() {
        return isClassAvailable(H2DriverClassName);
    }

    private static boolean isClassAvailable(String derbyDriverClassName) {
        try {
            Class.forName(derbyDriverClassName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Properties readOrmUnitProperties(Class<?> start) {
        return readOrmUnitProperties(start, new Properties());
    }

    public static Properties readOrmUnitProperties(Class<?> start, Properties d) {

        String[] split = start.getPackage().getName().split("\\.");
        int i = 0;
        StringBuilder path = new StringBuilder("/");
        Properties defaults = readDefaults(d);
        Properties result = null;
        try {
            do {
                InputStream propertiesStream = start.getResourceAsStream(path + ORMUnitPropertiesReader.PropertiesFileName);
                result = new Properties(defaults);

                if (propertiesStream != null)
                    result.load(propertiesStream);
                defaults = result;
                if (i < split.length)
                    path.append(split[i]).append("/");
                i++;
            } while (i <= split.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static Properties readDefaults(Properties d) {
        InputStream resourceAsStream = TestSet.class.getResourceAsStream("/" + ORMUnitPropertiesReader.DefaultPropertiesFileName);
        Properties properties = new Properties(d);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new ConfigurationException(e);
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
