package pl.kurs.anonymoussurveillance.commands;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmploymentCommand {
    @NonNull
    @NotEmpty
    private LocalDate startDate;

    private LocalDate endDate;

    @NonNull
    @NotEmpty
    private String companyName;

    @NonNull
    @NotEmpty
    private String role;

    @NonNull
    @NotEmpty
    private BigDecimal salary;
}
