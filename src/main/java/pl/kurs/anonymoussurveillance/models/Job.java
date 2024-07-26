package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "retirees")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_job")
    private Long id;
    private LocalDate startDate;
    private LocalDate finishDate;
    private String position;
    private BigDecimal salary;
}
