package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;

@Entity
@Table(name = "person_attribute")
public class PersonAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;

    public PersonAttribute(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public PersonAttribute() {

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

    public void setName(String key) {
        this.name = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
