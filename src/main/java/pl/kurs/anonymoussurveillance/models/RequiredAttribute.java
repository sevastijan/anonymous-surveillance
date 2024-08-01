package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "person_type_required_attributes")
public class RequiredAttribute implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private AttributeType attributeType;

    @ManyToOne
    @JoinColumn(name = "person_type_id")
    private PersonType personType;

    public RequiredAttribute(String name, AttributeType attributeType) {
        this.name = name;
        this.attributeType = attributeType;
    }

    public RequiredAttribute(String name, AttributeType attributeType, PersonType personType) {
        this.name = name;
        this.attributeType = attributeType;
        this.personType = personType;
    }
}
