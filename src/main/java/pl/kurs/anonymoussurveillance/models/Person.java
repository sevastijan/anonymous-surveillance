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
@Table(name = "person_list")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String pesel;

    @Column(nullable = false)
    private int height;

    @Column(nullable = false)
    private int weight;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_type_id", nullable = false)
    private PersonType personType;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonAttribute> attributes;

    // Builder class for constructing Person instances
    public static class Builder {
        private String firstName;
        private String lastName;
        private String pesel;
        private int height;
        private int weight;
        private String email;
        private PersonType personType;
        private List<PersonAttribute> attributes;

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setPesel(String pesel) {
            this.pesel = pesel;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPersonType(PersonType personType) {
            this.personType = personType;
            return this;
        }

        public Builder setAttributes(List<PersonAttribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Person build() {
            Person person = new Person();
            person.firstName = this.firstName;
            person.lastName = this.lastName;
            person.pesel = this.pesel;
            person.height = this.height;
            person.weight = this.weight;
            person.email = this.email;
            person.personType = this.personType;
            person.attributes = this.attributes;
            return person;
        }
    }
}
