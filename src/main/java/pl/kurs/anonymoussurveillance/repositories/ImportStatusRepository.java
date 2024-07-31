package pl.kurs.anonymoussurveillance.repositories;

import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kurs.anonymoussurveillance.models.ImportStatus;

@Repository
public interface ImportStatusRepository extends JpaRepository<ImportStatus, Long> {
}
