package org.ormunit.entity.embedded;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by IntelliJ IDEA.
 * User: jan
 * Date: 10.09.11
 * Time: 21:55
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class EmbeddingEntity {


    @Id
    private Integer id;

    @Override
    public String toString() {
        return "EmbeddingEntity{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }

    @Basic
    private EmbeddableClass value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EmbeddableClass getValue() {
        return value;
    }

    public void setValue(EmbeddableClass value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmbeddingEntity that = (EmbeddingEntity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
