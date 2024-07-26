package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "person_type")
public class PersonType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_type_id")
    private List<PersonAttribute> attributes;

    public PersonType(String name, List<PersonAttribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public PersonType() {
    }

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
