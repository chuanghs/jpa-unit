package org.ormunit.junit;

import com.sun.java.xml.ns.persistence.Persistence;
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

    private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");


    public static String getPersistenceProvider(Class<?> caller, String unitName) {
        Persistence persistenceUnit = getPersistenceUnit(caller);
        if (persistenceUnit != null) {
            for (Persistence.PersistenceUnit pu : persistenceUnit.getPersistenceUnit()) {
                if (pu.getName().equals(unitName)) {
                    return pu.getProvider();
                }
            }
        }
        return null;
    }

    public static Set<Class> getManagedTypes(Class<?> caller, String unitName) {
        Persistence cast = null;

        cast = getPersistenceUnit(caller);

        if (cast != null) {
            for (Persistence.PersistenceUnit pu : cast.getPersistenceUnit()) {
                if (pu.getName().equals(unitName)) {
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
            }
        }
        return null;
    }

    private static Persistence getPersistenceUnit(Class<?> caller) {
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
        return cast;
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

}
