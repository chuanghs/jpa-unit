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
 * public @interface Basic {
 * FetchType fetch() default EAGER;
 * boolean optional() default true;
 * }
 * <p/>
 * <p/>
 * <p/>
 * <p>Java class for basic complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="basic">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="column" type="{http://java.sun.com/xml/ns/persistence/orm}column" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="lob" type="{http://java.sun.com/xml/ns/persistence/orm}lob" minOccurs="0"/>
 *           &lt;element name="temporal" type="{http://java.sun.com/xml/ns/persistence/orm}temporal" minOccurs="0"/>
 *           &lt;element name="enumerated" type="{http://java.sun.com/xml/ns/persistence/orm}enumerated" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fetch" type="{http://java.sun.com/xml/ns/persistence/orm}fetch-type" />
 *       &lt;attribute name="optional" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "basic", propOrder = {
        "column",
        "lob",
        "temporal",
        "enumerated"
})
public class Basic {

    protected Column column;
    protected Lob lob;
    protected TemporalType temporal;
    protected EnumType enumerated;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected FetchType fetch;
    @XmlAttribute
    protected Boolean optional;
    @XmlAttribute
    protected AccessType access;

    /**
     * Gets the value of the column property.
     *
     * @return possible object is
     *         {@link Column }
     */
    public Column getColumn() {
        return column;
    }

    /**
     * Sets the value of the column property.
     *
     * @param value allowed object is
     *              {@link Column }
     */
    public void setColumn(Column value) {
        this.column = value;
    }

    /**
     * Gets the value of the lob property.
     *
     * @return possible object is
     *         {@link Lob }
     */
    public Lob getLob() {
        return lob;
    }

    /**
     * Sets the value of the lob property.
     *
     * @param value allowed object is
     *              {@link Lob }
     */
    public void setLob(Lob value) {
        this.lob = value;
    }

    /**
     * Gets the value of the temporal property.
     *
     * @return possible object is
     *         {@link TemporalType }
     */
    public TemporalType getTemporal() {
        return temporal;
    }

    /**
     * Sets the value of the temporal property.
     *
     * @param value allowed object is
     *              {@link TemporalType }
     */
    public void setTemporal(TemporalType value) {
        this.temporal = value;
    }

    /**
     * Gets the value of the enumerated property.
     *
     * @return possible object is
     *         {@link EnumType }
     */
    public EnumType getEnumerated() {
        return enumerated;
    }

    /**
     * Sets the value of the enumerated property.
     *
     * @param value allowed object is
     *              {@link EnumType }
     */
    public void setEnumerated(EnumType value) {
        this.enumerated = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the fetch property.
     *
     * @return possible object is
     *         {@link FetchType }
     */
    public FetchType getFetch() {
        return fetch;
    }

    /**
     * Sets the value of the fetch property.
     *
     * @param value allowed object is
     *              {@link FetchType }
     */
    public void setFetch(FetchType value) {
        this.fetch = value;
    }

    /**
     * Gets the value of the optional property.
     *
     * @return possible object is
     *         {@link Boolean }
     */
    public Boolean isOptional() {
        return optional;
    }

    /**
     * Sets the value of the optional property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setOptional(Boolean value) {
        this.optional = value;
    }

    /**
     * Gets the value of the access property.
     *
     * @return possible object is
     *         {@link AccessType }
     */
    public AccessType getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     *
     * @param value allowed object is
     *              {@link AccessType }
     */
    public void setAccess(AccessType value) {
        this.access = value;
    }

}
