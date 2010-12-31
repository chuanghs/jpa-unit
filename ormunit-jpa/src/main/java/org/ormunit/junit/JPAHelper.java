package org.ormunit.junit;

import com.sun.java.xml.ns.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 23:05
 */
public class JPAHelper {

    private static final Logger log = LoggerFactory.getLogger(JPAHelper.class);

    public static Set<Class> getManagedTypes(Class<?> caller, String unitName) {
        InputStream stream = null;
        try {
            JAXBContext context = JAXBContext.newInstance("com.sun.java.xml.ns.persistence");
            stream = caller.getResourceAsStream("/META-INF/persistence.xml");
            if (stream != null) {
                Unmarshaller unmarshaller = context.createUnmarshaller();

                XMLEventReader xer = XMLInputFactory.newInstance().createXMLEventReader(stream);

                Persistence cast = Persistence.class.cast(unmarshaller.unmarshal(xer));

                for (Persistence.PersistenceUnit pu : cast.getPersistenceUnit()) {
                    if (pu.getName().equals(unitName)) {
                        Set<Class> classes = new HashSet<Class>();
                        for (String cn : pu.getClazz()) {
                            classes.add(Class.forName(cn));
                        }
                        return classes;
                    }
                }
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
        return null;
    }

}
