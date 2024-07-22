package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employes")
public class Employee extends Person {
    private LocalDate employmentStartDate;
    private String currentPosition;
    private BigDecimal currentSalary;
    private List<Job> jobs;
}
