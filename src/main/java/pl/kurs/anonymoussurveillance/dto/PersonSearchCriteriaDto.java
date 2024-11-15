package pl.kurs.anonymoussurveillance.dto;


import jakarta.persistence.Column;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.pl.PESEL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PersonSearchCriteriaDto {
    private Long id;
    private String personType;
    private List<PersonAttributeCriteriaDto> attributes;
    private Map<String, Number[]> numberRange;
    private Map<String, LocalDate[]> dateRange;
    private String pesel;

    public PersonSearchCriteriaDto() {
        this.attributes = new ArrayList<>();
        this.numberRange = new HashMap<>();
        this.dateRange = new HashMap<>();
    }
}
