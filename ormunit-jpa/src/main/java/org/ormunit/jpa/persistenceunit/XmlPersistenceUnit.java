package org.ormunit.jpa.persistenceunit;

import com.sun.java.xml.ns.persistence.Persistence;
import com.sun.java.xml.ns.persistence.orm.Entity;
import com.sun.java.xml.ns.persistence.orm.EntityMappings;
import org.ormunit.exception.ConfigurationException;
import org.ormunit.jpa.entityinspector.AnnotationsEntityInspector;
import org.ormunit.jpa.entityinspector.EntityInspector;
import org.ormunit.jpa.entityinspector.EntityMappingsEntityInspector;
import org.ormunit.jpa.providerproperties.EclipseLinkProperties;
import org.ormunit.jpa.providerproperties.HibernatJPAProperties;
import org.ormunit.jpa.providerproperties.OpenJPAProperties;
import org.ormunit.jpa.providerproperties.ProviderProperties;
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
    private Persistence.PersistenceUnit persistenceUnit;
    private List<EntityMappings> ormMappings = new LinkedList<EntityMappings>();

    /**
     * @param caller   class whose classloader will be used to load persistence.xml
     * @param unitName persistence persistenceunit name of which managed types will be used
     */
    public XmlPersistenceUnit(Class<?> caller, String unitName) {
        this.caller = caller;
        this.persistenceUnit = getPersistenceUnitFromPersistenceXml(unitName);
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
                    throw new ConfigurationException(e);
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
     * @return set of Class objects representing all managed classes in given persistence persistenceunit
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
            throw new ConfigurationException(String.format("Error when unmarshaling %s", xmlFileName));
        } catch (JAXBException e) {
            throw new ConfigurationException(String.format("Error when unmarshaling %s", xmlFileName));
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


    private Persistence.PersistenceUnit getPersistenceUnitFromPersistenceXml(String unitName) {
        Persistence persistenceFileRoot = null;
        try {
            persistenceFileRoot = readPersistenceFile();
        } catch (FileNotFoundException e) {
            throw new ConfigurationException(e);
        }
        StringBuilder sb = new StringBuilder();
        for (Persistence.PersistenceUnit pu : persistenceFileRoot.getPersistenceUnit()) {
            if (sb.length()>0)
                sb.append(",");
            sb.append(pu.getName());
            if (pu.getName().equals(unitName)) {
                return pu;
            }
        }
        throw new ConfigurationException(String.format("Persistence unit %s could not be found. These persistence units are found: %s", unitName, sb.toString()));
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
            throw new ConfigurationException(format);
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
        EntityInspector delegatedInspector = new AnnotationsEntityInspector();
        List<EntityMappings> ormMappings = new ArrayList<EntityMappings>(this.ormMappings);
        Collections.reverse(ormMappings);
        for (EntityMappings mappings : ormMappings) {
            delegatedInspector = new EntityMappingsEntityInspector(mappings, delegatedInspector);
        }
        return delegatedInspector;
    }
}
