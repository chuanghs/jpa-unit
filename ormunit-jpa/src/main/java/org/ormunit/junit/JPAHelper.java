package org.ormunit.junit;

import com.sun.java.xml.ns.persistence.Persistence;
import org.ormunit.JPAORMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.spi.PersistenceProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 23:05
 */
public class JPAHelper {


    private static final Logger log = LoggerFactory.getLogger(JPAHelper.class);

    public static final String derbyDriverClassName = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String h2DriverClassName = "org.h2.Driver";
    public static final String hsqlDriverClassName = "org.hsql.jdbcDriver";


    private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");


    public static String getPersistenceProvider(Class<?> caller, String unitName) {
        Persistence.PersistenceUnit persistenceUnit = getPersistenceUnit(caller, unitName);
        if (persistenceUnit != null) {
            return persistenceUnit.getProvider();
        }
        return null;
    }

    public static Set<Class> getManagedTypes(Class<?> caller, String unitName) {
        Persistence cast = null;

        Persistence.PersistenceUnit pu = getPersistenceUnit(caller, unitName);

        if (pu != null) {

            Set<Class> classes = new HashSet<Class>();
            for (String cn : pu.getClazz()) {
                try {
                    classes.add(Class.forName(cn));
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage());
                }
            }
            return classes;

        }
        return null;
    }

    private static Persistence.PersistenceUnit getPersistenceUnit(Class<?> caller, String unitName) {
        InputStream stream = null;
        Persistence cast = null;
        try {
            JAXBContext context = JAXBContext.newInstance("com.sun.java.xml.ns.persistence");
            stream = caller.getResourceAsStream("/META-INF/persistence.xml");

            if (stream != null) {
                Unmarshaller unmarshaller = context.createUnmarshaller();

                XMLEventReader xer = XMLInputFactory.newInstance().createXMLEventReader(stream);

                cast = Persistence.class.cast(unmarshaller.unmarshal(xer));
            }
        } catch (Exception e) {
            log.error("", e);
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
        Persistence.PersistenceUnit result = null;

        for (Persistence.PersistenceUnit pu : cast.getPersistenceUnit()) {
            if (pu.getName().equals(unitName)) {
                result = pu;
                break;
            }
        }


        return result;
    }

    public static List<String> findAllProviders() {
        List<String> result = new LinkedList<String>();
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources =
                    loader.getResources("META-INF/services/" + PersistenceProvider.class.getName());
            Set<String> names = new HashSet<String>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                InputStream is = url.openStream();
                try {
                    names.addAll(providerNamesFromReader(new BufferedReader(new InputStreamReader(is))));
                } finally {
                    is.close();
                }
            }
            for (String s : names) {
                result.add(s);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }


    private static Set<String> providerNamesFromReader(BufferedReader reader) throws IOException {
        Set<String> names = new HashSet<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            Matcher m = nonCommentPattern.matcher(line);
            if (m.find()) {
                names.add(m.group().trim());
            }
        }
        return names;
    }

    public static boolean isDerby() {
        return isClassAvailable(derbyDriverClassName);
    }

    private static boolean isClassAvailable(String derbyDriverClassName) {
        try {
            Class.forName(derbyDriverClassName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isHSQL() {
        return isClassAvailable(hsqlDriverClassName);
    }

    public static boolean isH2() {
        return isClassAvailable(h2DriverClassName);
    }

    public static Properties getProperties(Class<?> aClass, String unitName, Properties defaults) {
        Properties result = new Properties(defaults);

        Persistence.PersistenceUnit.Properties properties = getPersistenceUnit(aClass, unitName).getProperties();
        for (Persistence.PersistenceUnit.Properties.Property p : properties.getProperty()) {
            result.setProperty(p.getName(), p.getValue());
        }

        return result;
    }
}
