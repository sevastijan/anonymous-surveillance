package pl.kurs.anonymoussurveillance.dto;

import lombok.Getter;
import lombok.Setter;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import java.util.List;

@Getter
@Setter
public class PersonTypeDto {
    private Long id;
    private String name;
}
