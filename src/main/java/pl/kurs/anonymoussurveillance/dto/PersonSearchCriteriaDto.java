package pl.kurs.anonymoussurveillance.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PersonSearchCriteriaDto {
    private Long id;
    private String personType;
    private List<PersonAttributeCriteriaDto> attributes;
    private Map<String, Number[]> numericRanges;
}
