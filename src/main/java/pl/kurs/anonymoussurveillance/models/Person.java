package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.pl.PESEL;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "person_list")
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicUpdate
public class Person implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Size(min = 11, max = 11, message = "PESEL must be exactly 11 digits")
    @Pattern(regexp = "\\d{11}", message = "PESEL must contain only digits")
    @PESEL
    private String pesel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_type_id", nullable = false)
    private PersonType personType;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PersonAttribute> attributes;

    @Version
    private Long version;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Employment> employment;

    public Person(PersonType personType, Set<PersonAttribute> attributes) {
        this.personType = personType;
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(personType, person.personType) && Objects.equals(attributes, person.attributes) && Objects.equals(version, person.version) && Objects.equals(employment, person.employment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personType, attributes, version, employment);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", personType=" + personType +
                ", attributes=" + attributes +
                ", version=" + version +
                ", employment=" + employment +
                '}';
    }
}
