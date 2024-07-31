package pl.kurs.anonymoussurveillance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.anonymoussurveillance.models.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
