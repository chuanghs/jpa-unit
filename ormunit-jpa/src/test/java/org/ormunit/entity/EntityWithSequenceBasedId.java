package org.ormunit.entity;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Date: 02.01.12 01:03
 *
 * @author: Tomasz Krzyżak
 */
@Table(schema = "table_schema")
@Entity
@SequenceGenerator(schema = "sequence_schema", sequenceName = "sequence_schema.123", name = "123")
public class EntityWithSequenceBasedId {

    
    @Id
    @Generated("123")
    private int id;

}
