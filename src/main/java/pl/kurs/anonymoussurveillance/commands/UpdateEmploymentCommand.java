package pl.kurs.anonymoussurveillance.commands;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateEmploymentCommand {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String companyName;
    private String role;
    private BigDecimal salary;
}
