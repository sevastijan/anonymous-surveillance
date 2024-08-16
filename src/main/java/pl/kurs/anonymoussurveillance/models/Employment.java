package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "employment_list")
public class Employment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String companyName;
    private String role;
    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    public Employment(LocalDate startDate, LocalDate endDate, String companyName, String role, BigDecimal salary, Person person) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.companyName = companyName;
        this.role = role;
        this.salary = salary;
        this.person = person;
    }
}
