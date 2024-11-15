package pl.kurs.anonymoussurveillance.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportServiceTest {

    @InjectMocks
    private ImportService importService;

    @Mock
    private ImportStatusRepository importStatusRepository;

    @Mock
    private PersonTypeRepository personTypeRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private byte[] csvData;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        try (InputStream inputStream = new ClassPathResource("people_records_sample.csv").getInputStream()) {
            csvData = inputStream.readAllBytes();

            System.out.println(new String(csvData));
        }
    }

    @Test
    public void shouldSuccessfullyImportSampleFle () throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", csvData);

        when(importStatusRepository.save(any(ImportStatus.class))).thenAnswer(invocation -> {
            ImportStatus status = invocation.getArgument(0);
            if (status.getId() == null) {
                status.setId(1L);
            }
            return status;
        });

        when(personTypeRepository.findByName(anyString())).thenReturn(Optional.of(new PersonType()));
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[]{1});
        when(transactionManager.getTransaction(any())).thenReturn(mock(org.springframework.transaction.TransactionStatus.class));
        when(importService.importFile(file)).thenReturn(1L);

        Long importId = importService.importFile(file);

        Thread.sleep(2000);

        System.out.println(importId);

        assertNotNull(importId, "Import ID should not be null");
        verify(importStatusRepository, atLeast(2)).save(any(ImportStatus.class)); // Initial save and updates
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenFileIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            importService.importFile(null);
        });
        assertEquals("File must not be null", exception.getMessage());
    }

    @Test
    public void shouldReturnImportInprogressWhileStartNewImport() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", csvData);

        importService.currentImportTask = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(5000); // Simulate long-running task
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            importService.importFile(file);
        });
        assertEquals("Import already in progress", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionErrorWhileProcessFile() throws Exception {
        BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(new java.io.ByteArrayInputStream(csvData)));
        ImportStatus importStatus = new ImportStatus();
        importStatus.setId(1L);

        when(personTypeRepository.findByName(anyString())).thenThrow(new RuntimeException("Database error"));


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            importService.processFile(reader, importStatus);
        });
        assertEquals("Database error", exception.getCause().getMessage());
        assertEquals(Status.FAILED, importStatus.getStatus());
    }

    @Test
    public void shouldReturnNotFoundStatus() {
        when(importStatusRepository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            importService.getImportStatus(999L);
        });
        assertEquals("Import not found", exception.getMessage());
    }
}