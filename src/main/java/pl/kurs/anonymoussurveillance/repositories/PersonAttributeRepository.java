package pl.kurs.anonymoussurveillance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

@Repository
public interface PersonAttributeRepository extends JpaRepository<PersonAttribute, Long> {

}
