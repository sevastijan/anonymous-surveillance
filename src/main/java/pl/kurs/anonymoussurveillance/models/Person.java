package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.validator.constraints.pl.PESEL;

import java.io.Serializable;

@MappedSuperclass
public abstract class Person implements Serializable {
    static final long serialVersionUID = 42L;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surname;
    @PESEL
    private String pesel;
    @Column(nullable = false)
    private int height;
    @Column(nullable = false)
    private int weight;
    @Column(nullable = false)
    private String email;


}
