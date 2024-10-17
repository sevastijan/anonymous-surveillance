package pl.kurs.anonymoussurveillance.commands;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmploymentCommand {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String companyName;
    private String role;
    private BigDecimal salary;
}
