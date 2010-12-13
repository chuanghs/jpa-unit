package org.jpaunit;

import org.jpaunit.exception.JPAUnitConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 20:56
 */
public class JPAUnitConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JPAUnitConfiguration.class);

    public static final String ClassNamePattern = "[$a-zA-Z_]+[$a-zA-Z_0-9]*(\\.[$a-zA-Z_]+[$a-zA-Z_0-9]*)*";

    private Map<String, String> imports = new HashMap<String, String>();

     private List<String> statements = new LinkedList<String>();

     public void addStatement(String nodeValue) {
        statements.add(nodeValue);
    }

    public void addStatement(Integer index, String nodeValue) {
        statements.add(index, nodeValue);
    }

    public void executeStatements(EntityManager entityManager) {
        for (String s : statements){
            entityManager.createNativeQuery(s).executeUpdate();
        }
    }

    public void addImport(String className, String alias) {
        if (imports.containsKey(alias)){
            if (!className.equals(imports.get(alias)))
                throw new JPAUnitConfigurationException("alias: "+alias+" is defined more than once ("+imports.get(className)+", "+className+")");
            else {
                if (log.isWarnEnabled())
                    log.warn("alias: "+alias+" is defined twice for the same class: "+className);
            }
        }

        if (!className.matches(ClassNamePattern))
            throw new JPAUnitConfigurationException("className: "+className+" is invalid class name");

        imports.put(alias, className);
    }


}
