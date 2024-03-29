//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.03 at 07:19:08 AM CET 
//


package org.hibernate.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sessionFactory",
    "security"
})
@XmlRootElement(name = "hibernate-configuration")
public class HibernateConfiguration {

    @XmlElement(name = "session-factory", required = true)
    protected SessionFactory sessionFactory;
    protected Security security;

    /**
     * Gets the value of the sessionFactory property.
     * 
     * @return
     *     possible object is
     *     {@link SessionFactory }
     *     
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Sets the value of the sessionFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link SessionFactory }
     *     
     */
    public void setSessionFactory(SessionFactory value) {
        this.sessionFactory = value;
    }

    /**
     * Gets the value of the security property.
     * 
     * @return
     *     possible object is
     *     {@link Security }
     *     
     */
    public Security getSecurity() {
        return security;
    }

    /**
     * Sets the value of the security property.
     * 
     * @param value
     *     allowed object is
     *     {@link Security }
     *     
     */
    public void setSecurity(Security value) {
        this.security = value;
    }

}
