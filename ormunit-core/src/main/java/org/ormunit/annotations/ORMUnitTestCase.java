package org.ormunit.annotations;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 25.02.11
 * Time: 11:48
 * <p/>
 * Annotation used to specify test environment: ORM configuration and Entities
 */
public @interface ORMUnitTestCase {

    /**
     * persitence unit name for JPA,
     * session factory hbm.xml file for hibernate
     * and whatever configuration element for other orm providers
     *
     * @return
     */
    String ormConfiguration();

    /**
     * file name of ormunit.xml file which contains entities definitions.
     * If left unset testcase class name concatenated with ".xml" will be used.
     *
     * @return
     */
    String ormUnitFileName() default "";

}
