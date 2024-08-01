package pl.kurs.anonymoussurveillance.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.pl.PESEL;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "person_list")
@Inheritance(strategy = InheritanceType.JOINED)
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_type_id", nullable = false)
    private PersonType personType;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonAttribute> attributes;

    public Person(PersonType personType, List<PersonAttribute> attributes) {
        this.personType = personType;
        this.attributes = attributes;
    }
}
