package pl.kurs.anonymoussurveillance.commands;

import lombok.Getter;
import lombok.Setter;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import java.util.List;

@Getter
@Setter
public class  CreatePersonTypeCommand {
    private String name;
    private List<PersonAttribute> attributes;

}
