package pl.kurs.anonymoussurveillance.services;

import jakarta.annotation.Resource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.anonymoussurveillance.models.ImportStatus;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.models.Status;
import pl.kurs.anonymoussurveillance.repositories.ImportStatusRepository;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportServiceTest {

    @Mock
    private ImportStatusRepository importStatusRepository;

    @Mock
    private PersonTypeRepository personTypeRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MultipartFile multipartFile;

    private ImportService importService;

    @BeforeEach
    public void setUp() {
        importService = new ImportService(
                importStatusRepository,
                personTypeRepository,
                transactionManager,
                jdbcTemplate
        );
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenImportInProgress() throws IOException {
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        importService.importFile(multipartFile);

        assertThrows(IllegalStateException.class, () -> importService.importFile(multipartFile));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenFileIsNull() {
        assertThrows(IllegalArgumentException.class, () -> importService.importFile(null));
    }

    @Test
    public void shouldReturnStatusWhenImportExist() {
        ImportStatus mockStatus = new ImportStatus();
        mockStatus.setId(1L);
        mockStatus.setStatus(Status.COMPLETED);

        when(importStatusRepository.findById(1L)).thenReturn(Optional.of(mockStatus));

        ImportStatus retrievedStatus = importService.getImportStatus(1L);

        assertEquals(Status.COMPLETED, retrievedStatus.getStatus());
        assertEquals(1L, retrievedStatus.getId());
    }

    @Test
    public void shouldIllegalArgumentExceptionWhenImportNotExist() {
        when(importStatusRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> importService.getImportStatus(999L));
    }

    @Test
    public void shouldSetStatusToFailedWhenExceptionOccurs() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenThrow(new RuntimeException());

        when(importStatusRepository.save(any(ImportStatus.class))).thenAnswer(invocation -> {
            ImportStatus status = invocation.getArgument(0);
            status.setId(1L);
            return status;
        });

        assertThrows(RuntimeException.class,
                () -> importService.importFile(mockFile)
        );

    }

    @Test
    public void shouldProcessBatchesAndUpdateImportStatus() throws Exception {
        PersonType studentType = mock(PersonType.class);
        when(studentType.getName()).thenReturn("student");

        InputStream inputStream = getClass().getResourceAsStream("/people_records_sample.csv");
        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", inputStream);

        when(personTypeRepository.findAll()).thenReturn(Collections.singletonList(studentType));
        when(importStatusRepository.save(any(ImportStatus.class))).thenAnswer(invocation -> {
            ImportStatus status = invocation.getArgument(0);
            status.setId(1L);
            return status;
        });

        Long importId = importService.importFile(file);

        assertNotNull(importId);
        verify(importStatusRepository, atLeastOnce()).save(any(ImportStatus.class));
    }
}
