package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "students")
public class Student extends Person {    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_student")
    private Long id;

    private String university;
    private int graduationYear;
    private String major;
    private BigDecimal scholarshipAmount;

}
