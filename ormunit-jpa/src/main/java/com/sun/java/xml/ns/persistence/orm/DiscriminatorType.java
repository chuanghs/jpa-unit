//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.08 at 10:39:21 PM CEST 
//


package com.sun.java.xml.ns.persistence.orm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for discriminator-type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;simpleType name="discriminator-type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="STRING"/>
 *     &lt;enumeration value="CHAR"/>
 *     &lt;enumeration value="INTEGER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "discriminator-type")
@XmlEnum
public enum DiscriminatorType {

    STRING,
    CHAR,
    INTEGER;

    public String value() {
        return name();
    }

    public static DiscriminatorType fromValue(String v) {
        return valueOf(v);
    }

}
