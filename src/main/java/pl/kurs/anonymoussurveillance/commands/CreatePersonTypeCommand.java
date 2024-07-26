package pl.kurs.anonymoussurveillance.commands;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import java.util.List;

public class CreatePersonTypeCommand {
    private String name;
    private List<PersonAttribute> attributes;

    public String getName() {
        return name;
    }

    public List<PersonAttribute> getAttributes() {
        return attributes;
    }
}
