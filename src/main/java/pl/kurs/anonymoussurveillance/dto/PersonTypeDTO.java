package pl.kurs.anonymoussurveillance.dto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import java.util.List;

public class PersonTypeDTO {
    private Long id;
    private String name;
    private List<PersonAttribute> attributes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PersonAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PersonAttribute> attributes) {
        this.attributes = attributes;
    }
}
