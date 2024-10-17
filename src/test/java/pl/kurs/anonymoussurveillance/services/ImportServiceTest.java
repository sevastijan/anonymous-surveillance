package pl.kurs.anonymoussurveillance.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.anonymoussurveillance.factories.BatchProcessingServiceFactory;
import pl.kurs.anonymoussurveillance.models.ImportStatus;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.Status;
import pl.kurs.anonymoussurveillance.repositories.ImportStatusRepository;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ImportServiceTest {

    private ImportService importService;

    @Mock
    private ImportStatusRepository importStatusRepository;

    @Mock
    private BatchProcessingServiceFactory batchProcessingServiceFactory;

    @Mock
    private ForkJoinPool forkJoinPool;

    @Captor
    private ArgumentCaptor<ImportStatus> importStatusCaptor;

    @Mock
    private MultipartFile file;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        importService = new ImportService(importStatusRepository, batchProcessingServiceFactory, forkJoinPool);
    }

    @Test
    public void shouldImportStatusChangeDuringImport() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/people_records_sample.csv");
        assertNotNull(inputStream, "Sample CSV file not found");

        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", inputStream);

        List<ImportStatus> savedStatuses = new ArrayList<>();

        when(importStatusRepository.save(any(ImportStatus.class))).thenAnswer(invocation -> {
            ImportStatus status = invocation.getArgument(0);

            if (status.getId() == null) {
                status.setId(1L);
            }

            ImportStatus copy = new ImportStatus();
            copy.setId(status.getId());
            copy.setStatus(status.getStatus());
            copy.setCreatedDate(status.getCreatedDate());
            copy.setStartDate(status.getStartDate());
            copy.setEndDate(status.getEndDate());
            copy.setProcessedRows(status.getProcessedRows());
            copy.setRowsPerSecond(status.getRowsPerSecond());
            copy.setErrorMessage(status.getErrorMessage());

            savedStatuses.add(copy);
            return status;
        });

        ImportService importServiceSpy = spy(importService);
        doReturn(new ArrayList<Person>()).when(importServiceSpy).processFile(any(MultipartFile.class), any(ImportStatus.class));

        when(forkJoinPool.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            ForkJoinTask<?> forkJoinTask = ForkJoinTask.adapt(task);
            forkJoinTask.invoke();
            return forkJoinTask;
        });

        Long importId = importServiceSpy.importFile(file);

        assertEquals(1L, importId);

        assertTrue(savedStatuses.size() >= 3, "Expected at least 3 saves of ImportStatus");
        assertEquals(Status.PENDING, savedStatuses.get(0).getStatus());

        assertEquals(Status.IN_PROGRESS, savedStatuses.get(1).getStatus());

        ImportStatus finalStatus = savedStatuses.get(savedStatuses.size() - 1);
        assertEquals(Status.COMPLETED, finalStatus.getStatus());
        assertEquals(0, finalStatus.getProcessedRows());
        assertNotNull(finalStatus.getStartDate());
        assertNotNull(finalStatus.getEndDate());
    }

    @Test
    public void shouldThrowExceptionWhenFileIsNull() {
        MultipartFile file = null;

        assertThrows(IllegalArgumentException.class, () -> {
            importService.importFile(file);
        });
    }

    @Test
    public void shouldThrowExceptionWhenImportIsAlreadyInProgress() {
        importService.currentImportTask = mock(Future.class);
        when(importService.currentImportTask.isDone()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> {
            importService.importFile(file);
        });
    }

    @Test
    public void shouldHandleFailedStatusDuringImport() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/people_records_sample.csv");
        assertNotNull(inputStream, "Sample CSV file not found");

        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", inputStream);

        List<ImportStatus> savedStatuses = new ArrayList<>();

        when(importStatusRepository.save(any(ImportStatus.class))).thenAnswer(invocation -> {
            ImportStatus status = invocation.getArgument(0);

            if (status.getId() == null) {
                status.setId(1L);
            }

            ImportStatus copy = new ImportStatus();
            copy.setId(status.getId());
            copy.setStatus(status.getStatus());
            copy.setCreatedDate(status.getCreatedDate());
            copy.setStartDate(status.getStartDate());
            copy.setEndDate(status.getEndDate());
            copy.setProcessedRows(status.getProcessedRows());
            copy.setRowsPerSecond(status.getRowsPerSecond());
            copy.setErrorMessage(status.getErrorMessage());
            savedStatuses.add(copy);
            return status;
        });

        ImportService importServiceSpy = spy(importService);
        doThrow(new RuntimeException("Failing test exception")).when(importServiceSpy).processFile(any(MultipartFile.class), any(ImportStatus.class));

        when(forkJoinPool.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            ForkJoinTask<?> forkJoinTask = ForkJoinTask.adapt(task);
            forkJoinTask.invoke();
            return forkJoinTask;
        });

        assertThrows(RuntimeException.class, () -> {
            importServiceSpy.importFile(file);
        });

        assertTrue(savedStatuses.size() >= 3, "Expected at least 3 saves of ImportStatus");

        assertEquals(Status.PENDING, savedStatuses.get(0).getStatus());
        assertEquals(Status.IN_PROGRESS, savedStatuses.get(1).getStatus());

        ImportStatus failedStatus = savedStatuses.get(savedStatuses.size() - 1);
        assertEquals(Status.FAILED, failedStatus.getStatus());
        assertEquals("Failing test exception", failedStatus.getErrorMessage());
        assertNotNull(failedStatus.getStartDate());
        assertNotNull(failedStatus.getEndDate());
    }

    @Test
    public void shouldProcessFileSuccessfully() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/people_records_sample.csv");
        assertNotNull(inputStream, "Sample CSV file not found");

        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", inputStream);

        ImportStatus importStatus = new ImportStatus();

        InputStream inputStreamForCsvRecords = getClass().getResourceAsStream("/people_records_sample.csv");
        assertNotNull(inputStreamForCsvRecords, "Sample CSV file not found");

        List<CSVRecord> csvRecords = new ArrayList<>();
        CSVFormat.DEFAULT.withFirstRecordAsHeader()
                .parse(new InputStreamReader(inputStreamForCsvRecords))
                .forEach(csvRecords::add);

        BatchProcessingService batchProcessingService = mock(BatchProcessingService.class);
        List<Person> personList = Arrays.asList(new Person(), new Person());
        when(batchProcessingService.join()).thenReturn(personList);

        when(batchProcessingServiceFactory.createBatchProcessingService(anyList(), eq(importStatus), eq(0), eq(csvRecords.size())))
                .thenReturn(batchProcessingService);

        when(forkJoinPool.submit(any(BatchProcessingService.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Person> result = importService.processFile(file, importStatus);

        assertEquals(2, result.size());
        verify(batchProcessingServiceFactory).createBatchProcessingService(anyList(), eq(importStatus), eq(0), eq(csvRecords.size()));
    }


    @Test
    public void shouldThrowExceptionWhenProcessingFileFails() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/people_records_sample.csv");
        assertNotNull(inputStream, "Sample CSV file not found");

        MockMultipartFile file = new MockMultipartFile("file", "people_records_sample.csv", "text/csv", inputStream);

        ImportStatus importStatus = new ImportStatus();

        when(batchProcessingServiceFactory.createBatchProcessingService(anyList(), eq(importStatus), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Processing failed"));

        assertThrows(RuntimeException.class, () -> {
            importService.processFile(file, importStatus);
        });
    }

    @Test
    public void shouldGetImportStatusSuccessfully() {
        Long importId = 1L;
        ImportStatus importStatus = new ImportStatus();
        importStatus.setId(importId);

        when(importStatusRepository.findById(importId)).thenReturn(Optional.of(importStatus));

        ImportStatus result = importService.getImportStatus(importId);

        assertNotNull(result);
        assertEquals(importId, result.getId());
    }

    @Test
    public void shouldThrowExceptionWhenImportStatusNotFound() {
        Long importId = 1L;

        when(importStatusRepository.findById(importId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            importService.getImportStatus(importId);
        });

        assertEquals("Import not found", exception.getMessage());
    }
}
