package pl.kurs.anonymoussurveillance.factories;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import pl.kurs.anonymoussurveillance.models.ImportStatus;
import pl.kurs.anonymoussurveillance.repositories.*;
import pl.kurs.anonymoussurveillance.services.BatchProcessingService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BatchProcessingServiceFactory {
    private final PersonRepository personRepository;
    private final PersonTypeRepository personTypeRepository;
    private final PersonAttributeRepository personAttributeRepository;
    private final EmploymentRepository employmentRepository;
    private final ImportStatusRepository importStatusRepository;

    public BatchProcessingService createBatchProcessingService(List<CSVRecord> records, ImportStatus importStatus, int start, int end) {
        return new BatchProcessingService(records, importStatus, start, end,
                personRepository, personTypeRepository,
                personAttributeRepository, employmentRepository,
                importStatusRepository, this);

    }
}
