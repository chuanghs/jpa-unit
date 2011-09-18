package org.ormunit.jpa.annotations;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 25.02.11
 * Time: 11:48
 * <p/>
 * Annotation used to specify test environment: ORM configuration and Entities
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JPAUnitTestCase {

    /**
     * persitence persistenceunit name for JPA,
     * session factory hbm.xml file for hibernate
     * and whatever configuration element for other orm providers
     *
     * @return
     */
    String unitName();

    /**
     * file name of ormunit.xml file which contains entities definitions.
     * If left unset testcase class name concatenated with ".xml" will be used.
     *
     * @return
     */
    String ormUnitFileName() default "";

}
