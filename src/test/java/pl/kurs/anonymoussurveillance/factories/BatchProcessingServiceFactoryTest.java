package pl.kurs.anonymoussurveillance.factories;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kurs.anonymoussurveillance.factories.BatchProcessingServiceFactory;
import pl.kurs.anonymoussurveillance.models.ImportStatus;
import pl.kurs.anonymoussurveillance.repositories.*;
import pl.kurs.anonymoussurveillance.services.BatchProcessingService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BatchProcessingServiceFactoryTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private PersonTypeRepository personTypeRepository;
    @Mock
    private PersonAttributeRepository personAttributeRepository;
    @Mock
    private EmploymentRepository employmentRepository;
    @Mock
    private ImportStatusRepository importStatusRepository;

    private BatchProcessingServiceFactory factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factory = new BatchProcessingServiceFactory(
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository
        );
    }

    @Test
    public void shouldCreateBatchProcessingService() {
        List<CSVRecord> records = List.of();
        ImportStatus importStatus = new ImportStatus();
        int start = 0;
        int end = 10;

        BatchProcessingService service = factory.createBatchProcessingService(records, importStatus, start, end);

        assertNotNull(service);
    }
}
