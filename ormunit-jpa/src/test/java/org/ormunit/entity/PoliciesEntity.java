package org.ormunit.entity;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 14.02.11
 * Time: 08:06
 */

@Entity
@Table(name="policiesentity", schema="policies")
public class PoliciesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    private String field;
}
