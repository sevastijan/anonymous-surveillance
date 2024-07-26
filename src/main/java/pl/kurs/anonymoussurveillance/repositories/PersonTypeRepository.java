package pl.kurs.anonymoussurveillance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kurs.anonymoussurveillance.models.PersonType;

import java.util.Optional;

@Repository
public interface PersonTypeRepository extends JpaRepository<PersonType, Long> {
    Optional<PersonType> findByName(String name);
}
