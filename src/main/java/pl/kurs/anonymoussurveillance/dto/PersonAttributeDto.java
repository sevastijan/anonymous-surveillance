package pl.kurs.anonymoussurveillance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonAttributeDto {
    private Long id;
    private String name;
    private String value;
}
