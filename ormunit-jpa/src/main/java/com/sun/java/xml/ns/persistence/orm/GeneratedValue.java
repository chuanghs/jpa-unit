//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.08 at 10:39:21 PM CEST 
//


package com.sun.java.xml.ns.persistence.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface GeneratedValue {
 * GenerationType strategy() default AUTO;
 * String generator() default "";
 * }
 * <p/>
 * <p/>
 * <p/>
 * <p>Java class for generated-value complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="generated-value">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="strategy" type="{http://java.sun.com/xml/ns/persistence/orm}generation-type" />
 *       &lt;attribute name="generator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generated-value")
public class GeneratedValue {

    @XmlAttribute
    protected GenerationType strategy;
    @XmlAttribute
    protected String generator;

    /**
     * Gets the value of the strategy property.
     *
     * @return possible object is
     *         {@link GenerationType }
     */
    public GenerationType getStrategy() {
        return strategy;
    }

    /**
     * Sets the value of the strategy property.
     *
     * @param value allowed object is
     *              {@link GenerationType }
     */
    public void setStrategy(GenerationType value) {
        this.strategy = value;
    }

    /**
     * Gets the value of the generator property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getGenerator() {
        return generator;
    }

    /**
     * Sets the value of the generator property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGenerator(String value) {
        this.generator = value;
    }

}
