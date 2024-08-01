package pl.kurs.anonymoussurveillance.dto;

import lombok.Getter;
import lombok.Setter;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import java.util.List;

@Getter
@Setter
public class PersonDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String pesel;
    private String email;
    private PersonTypeDto personType;
    private List<PersonAttributeDto> attributes;
}
