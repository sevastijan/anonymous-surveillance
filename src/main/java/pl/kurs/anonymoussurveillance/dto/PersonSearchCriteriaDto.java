package pl.kurs.anonymoussurveillance.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class PersonSearchCriteriaDto {
    private Long id;
    private String personType;
    private List<PersonAttributeCriteriaDto> attributes;
    private Map<String, Number[]> numberRange;
    private Map<String, LocalDate[]> dateRange;
}
