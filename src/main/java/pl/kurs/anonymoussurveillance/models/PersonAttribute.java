package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "person_attributes")
public class PersonAttribute implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_attr_seq")
    @SequenceGenerator(name = "person_attr_seq", sequenceName = "seq_person_attr", initialValue = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private AttributeType type;

    private String value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_type_id")
    private PersonType personType;

    public PersonAttribute(String name, AttributeType type, String value, Person person, PersonType personType) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.person = person;
        this.personType = personType;
    }

    @Override
    public String toString() {
        return "PersonAttribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
