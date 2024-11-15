package pl.kurs.anonymoussurveillance.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kurs.anonymoussurveillance.models.Person;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
    @Query("SELECT p FROM Person p LEFT JOIN FETCH p.attributes WHERE p.id = :id")
    Optional<Person> findByIdWithAttributes(@Param("id") Long id);
}
