package pl.kurs.anonymoussurveillance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.anonymoussurveillance.models.EmploymentHistory;

import java.util.Optional;

public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistory, Long> {
    Optional<EmploymentHistory> findByIdAndPersonId(Long id, Long personId);
}
