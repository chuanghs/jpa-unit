package org.ormunit.entity;

import javax.persistence.*;

@Entity
public class PhoneNumber {

    @Id
    private Integer id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "E_ID", referencedColumnName = "E_ID"),
            @JoinColumn(name = "E_COUNTRY", referencedColumnName = "COUNTRY")
    })

    private Employee contact;

    public Employee getContact() {
        return contact;
    }
}