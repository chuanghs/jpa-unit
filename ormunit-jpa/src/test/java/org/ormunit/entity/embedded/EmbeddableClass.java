package org.ormunit.entity.embedded;

import javax.persistence.Embeddable;

/**
 * Created by IntelliJ IDEA.
 * User: jan
 * Date: 10.09.11
 * Time: 21:53
 * To change this template use File | Settings | File Templates.
 */
@Embeddable
public class EmbeddableClass {

    private String embeddedProperty;

    public String getEmbeddedProperty() {
        return embeddedProperty;
    }

    public void setEmbeddedProperty(String embeddedProperty) {
        this.embeddedProperty = embeddedProperty;
    }


    @Override
    public String toString() {
        return "EmbeddableClass{" +
                "'" + embeddedProperty + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmbeddableClass that = (EmbeddableClass) o;

        if (embeddedProperty != null ? !embeddedProperty.equals(that.embeddedProperty) : that.embeddedProperty != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return embeddedProperty != null ? embeddedProperty.hashCode() : 0;
    }
}
