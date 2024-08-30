package pl.kurs.anonymoussurveillance.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "person_types")
public class PersonType implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "personType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RequiredAttribute> requiredAttributes;

    public PersonType(String name, List<RequiredAttribute> requiredAttributes) {
        this.name = name;
        this.requiredAttributes = requiredAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonType that = (PersonType) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(requiredAttributes, that.requiredAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, requiredAttributes);
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
