package org.ormunit.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@IdClass(EmployeeId.class)
public class Employee {

    @Id
    @Column(name = "E_ID")
    private Integer eId;

    @Id
    private String country;

    @OneToMany(mappedBy = "contact")
    private List<PhoneNumber> contactNumber;

    public List<PhoneNumber> getContactNumber() {
        return contactNumber;
    }

    public String getCountry() {
        return country;
    }

    public Integer geteId() {
        return eId;
    }
}