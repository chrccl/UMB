package it.chrccl.umb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class User {

    @Id
    @Column(name = "mobile_phone")
    private String mobilePhone;

    private String fullName;

    private String age;

    private String sex;

}
