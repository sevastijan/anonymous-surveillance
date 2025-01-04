package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "person_attributes")
public class PersonAttribute implements Serializable, RootAware<Person> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_attr_seq")
//    @SequenceGenerator(name = "person_attr_seq", sequenceName = "seq_person_attr", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private AttributeType type;

    private String attribute_value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    public PersonAttribute(String name, AttributeType type, String attribute_value, Person person) {
        this.name = name;
        this.type = type;
        this.attribute_value = attribute_value;
        this.person = person;
    }

    @Override
    public Person root() {
        return person;
    }

    @Override
    public String toString() {
        return "PersonAttribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", value='" + attribute_value + '\'' +
                ", person=" + person +
                '}';
    }
}
