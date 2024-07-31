package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "person_types")
public class PersonType implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "personType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Person> personList;

    @OneToMany(mappedBy = "personType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RequiredAttribute> requiredAttributes;

    public PersonType(String name, List<RequiredAttribute> requiredAttributes) {
        this.name = name;
        this.requiredAttributes = requiredAttributes;
    }

    @Override
    public String toString() {
        return "PersonType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", requiredAttributes=" + requiredAttributes +
                '}';
    }
}
