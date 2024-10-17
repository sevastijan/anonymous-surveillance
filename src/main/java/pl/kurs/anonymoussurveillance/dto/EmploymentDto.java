package pl.kurs.anonymoussurveillance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String companyName;
    private String role;
    private BigDecimal salary;
}
