package org.ormunit.entity;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 10:48
 *
 */
@Entity
@Table(name = "propertyaccessentity", schema = "testschema")
public class PropertyAccessEntity {

    private Integer id;

    private String someProperty;

    private String justGetterProperty;
    private String justSetterProperty;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSomeProperty(String someProperty) {
        this.someProperty = someProperty;
    }

    public String getSomeProperty() {
        return someProperty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyAccessEntity that = (PropertyAccessEntity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PropertyAccessEntity{" +
                "id=" + id +
                '}';
    }
    @Transient
    public String getJustGetterProperty() {
        return justGetterProperty;
    }

    @Transient
    public void setJustSetterProperty(String justSetterProperty) {
        this.justSetterProperty = justSetterProperty;
    }
}
