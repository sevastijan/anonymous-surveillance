package pl.kurs.anonymoussurveillance.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class PersonAttributeCriteriaDto {
    private Long id;
    private String name;
    private String value;
}
