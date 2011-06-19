package org.ormunit.unit;

import com.sun.java.xml.ns.persistence.Persistence;
import com.sun.java.xml.ns.persistence.orm.Entity;
import com.sun.java.xml.ns.persistence.orm.EntityMappings;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.inspector.AnnotationsEntityInspector;
import org.ormunit.inspector.EntityInspector;
import org.ormunit.provider.EclipseLinkProperties;
import org.ormunit.provider.HibernatJPAProperties;
import org.ormunit.provider.OpenJPAProperties;
import org.ormunit.provider.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.spi.PersistenceProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
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
public class XmlPersistenceUnit implements PersistenceUnit {

    private static final Logger log = LoggerFactory.getLogger(XmlPersistenceUnit.class);

    private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");


    public static final String PERSISTENCE_XML_FILENAME = "META-INF/persistence.xml";
    public static final String DEFAULT_ORM_XML_FILENAME = "/META-INF/orm.xml";

    private Class<?> caller;
    private String unitName;
    private Persistence.PersistenceUnit persistenceUnit;
    private List<EntityMappings> ormMappings = new LinkedList<EntityMappings>();

    /**
     * @param caller   class whose classloader will be used to load persistence.xml
     * @param unitName persistence unit name of which managed types will be used
     */
    public XmlPersistenceUnit(Class<?> caller, String unitName) {
        this.caller = caller;
        this.unitName = unitName;
        this.persistenceUnit = getPersistenceUnitFromPersistenceXml();
        List<String> ormFiles = this.persistenceUnit.getMappingFile();
        boolean defaultIncluded = false;
        if (ormFiles != null) {
            for (String ormFileName : ormFiles) {
                ormFileName = normalizeJarEntryPath(ormFileName);
                if (ormFileName.equals(DEFAULT_ORM_XML_FILENAME))
                    defaultIncluded = true;
                try {
                    ormMappings.add(readOrmFile(ormFileName));
                } catch (FileNotFoundException e) {
                    throw new ORMUnitConfigurationException(e);
                }
            }
        }
        if (!defaultIncluded) {
            try {
                ormMappings.add(readOrmFile(DEFAULT_ORM_XML_FILENAME));
            } catch (FileNotFoundException e) {
                // do nothing because default orm file is not required
            }
        }
    }

    public String getUnitName() {
        return unitName;
    }

    public String getPersistenceProvider() {
        String persistenceProvider = null;
        if (persistenceUnit != null) {
            persistenceProvider = persistenceUnit.getProvider();
        }
        if (persistenceProvider == null) {
            List<String> foundProviders = findAllProviders();
            if (foundProviders.size() > 0) {
                persistenceProvider = foundProviders.get(0);
            }
        }
        return persistenceProvider;
    }

    /**
     * @return set of Class objects representing all managed classes in given persistence unit
     */
    public Set<Class<?>> getManagedTypes() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.addAll(getManagedTypesFromPersistenceUnit());
        for (EntityMappings em : ormMappings) {
            classes.addAll(getManagedTypesFromEntityMappings(em));
        }
        return classes;

    }

    public Properties getProperties(Properties defaults) {
        Properties result = new Properties(defaults);

        Persistence.PersistenceUnit.Properties properties = persistenceUnit.getProperties();
        if (properties != null)
            for (Persistence.PersistenceUnit.Properties.Property p : properties.getProperty()) {
                result.setProperty(p.getName(), p.getValue());
            }

        return result;
    }

    private EntityMappings readOrmFile(String ormFileName) throws FileNotFoundException {
        return readJAXBFile(caller, ormFileName, EntityMappings.class);
    }

    private Persistence readPersistenceFile() throws FileNotFoundException {
        return readJAXBFile(caller, PERSISTENCE_XML_FILENAME, Persistence.class);
    }

    private static <T> T readJAXBFile(Class<?> caller, String xmlFileName, Class<T> rootElementClass) throws FileNotFoundException {
        InputStream stream = null;
        try {
            stream = caller.getResourceAsStream(normalizeJarEntryPath(xmlFileName));

            if (stream != null) {

                JAXBContext context = JAXBContext.newInstance(rootElementClass);
                Unmarshaller unmarshaller = context.createUnmarshaller();

                XMLEventReader xer = XMLInputFactory.newInstance().createXMLEventReader(stream);
                return unmarshaller.unmarshal(xer, rootElementClass).getValue();
            } else {
                throw new FileNotFoundException(String.format("File not found %s", xmlFileName));
            }
        } catch (XMLStreamException e) {
            throw new ORMUnitConfigurationException(String.format("Error when unmarshaling %s", xmlFileName));
        } catch (JAXBException e) {
            throw new ORMUnitConfigurationException(String.format("Error when unmarshaling %s", xmlFileName));
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }


    private Persistence.PersistenceUnit getPersistenceUnitFromPersistenceXml() {
        Persistence persistenceFileRoot = null;
        try {
            persistenceFileRoot = readPersistenceFile();
        } catch (FileNotFoundException e) {
            throw new ORMUnitConfigurationException(e);
        }
        for (Persistence.PersistenceUnit pu : persistenceFileRoot.getPersistenceUnit()) {
            if (pu.getName().equals(unitName)) {
                return pu;
            }
        }
        return null;
    }


    private Set<Class<?>> getManagedTypesFromPersistenceUnit() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        if (persistenceUnit.getClazz() != null) {
            for (String cn : persistenceUnit.getClazz()) {
                classes.add(instatiateClass(cn));
            }
        }
        return classes;
    }


    private Collection<Class<?>> getManagedTypesFromEntityMappings(EntityMappings entityMappings) {
        Collection<Class<?>> result = new LinkedList<Class<?>>();
        if (entityMappings != null) {
            String package_ = entityMappings.getPackage() != null ? entityMappings.getPackage() + "." : "";
            if (entityMappings.getEntity() != null) {
                for (Entity entity : entityMappings.getEntity()) {
                    result.add(instatiateClass(package_ + entity.getClazz()));
                }
            }
        }
        return result;
    }

    private static String normalizeJarEntryPath(String ormFileName) {
        ormFileName = ormFileName.replaceAll("\\\\", "/");
        if ('/' != ormFileName.charAt(0))
            ormFileName = "/" + ormFileName;
        return ormFileName;
    }

    private static Class<?> instatiateClass(String cn) {
        Class<?> c;
        try {
            c = Class.forName(cn);
        } catch (ClassNotFoundException e) {
            String format = String.format("Class %s cannot be found", cn);
            log.error(format);
            throw new ORMUnitConfigurationException(format);
        }
        return c;
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


    public ProviderProperties createProviderProperties(Properties defaultDataSourceProperties) {
        String provider = getPersistenceProvider();
        if (EclipseLinkProperties.ProviderClassName.equals(provider)) {
            return new EclipseLinkProperties(this, defaultDataSourceProperties);
        } else if (OpenJPAProperties.providerClassName.equals(provider)) {
            return new OpenJPAProperties(this, defaultDataSourceProperties);
        } else if (HibernatJPAProperties.ProviderClassName.equals(provider)) {
            return new HibernatJPAProperties(this, defaultDataSourceProperties);
        } else
            throw new RuntimeException("unknown Persistence Provider");
    }

    public EntityInspector createClassInspector() {
        // TODO: change to entity inspector which operates both on annotations and orm.xml files
        return new AnnotationsEntityInspector();
    }
}
