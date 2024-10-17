package pl.kurs.anonymoussurveillance.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.anonymoussurveillance.factories.BatchProcessingServiceFactory;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportService {
    private final ImportStatusRepository importStatusRepository;
    private final BatchProcessingServiceFactory batchProcessingServiceFactory;
    private final ForkJoinPool forkJoinPool;

    Future<?> currentImportTask;

    @Transactional
    public Long importFile(MultipartFile file) {
        if(file == null) {
            throw new IllegalArgumentException("File must not be null");
        }

        if (currentImportTask != null && !currentImportTask.isDone()) {
            throw new IllegalStateException("An import is already in progress");
        }

        ImportStatus importStatus = new ImportStatus();
        importStatus.setStatus(Status.PENDING);
        importStatus.setCreatedDate(LocalDateTime.now());
        importStatusRepository.save(importStatus);

        BatchProcessingService.hasErrorOccurred.set(false);

        try {
            currentImportTask = forkJoinPool.submit(() -> {
                try {
                    importStatus.setStatus(Status.IN_PROGRESS);
                    importStatus.setStartDate(LocalDateTime.now());
                    importStatusRepository.save(importStatus);

                    List<Person> personList = processFile(file, importStatus);

                    importStatus.setProcessedRows(personList.size());
                    importStatus.setStatus(Status.COMPLETED);
                    if (BatchProcessingService.hasErrorOccurred.get()) {
                        throw new RuntimeException("Import failed due to validation error");
                    }

                } catch (Exception e) {
                    importStatus.setStatus(Status.FAILED);
                    importStatus.setErrorMessage(e.getMessage());

                    e.printStackTrace();

                    importStatus.setEndDate(LocalDateTime.now());
                    importStatusRepository.save(importStatus);
                    throw new RuntimeException(e.getMessage());
                } finally {
                    importStatus.setEndDate(LocalDateTime.now());
                    double timeDifference = java.time.Duration.between(importStatus.getStartDate(), importStatus.getEndDate()).toMillis() / 1000.0;
                    double rowsPerSecond = importStatus.getProcessedRows() / timeDifference;

                    importStatus.setRowsPerSecond(rowsPerSecond);
                    importStatusRepository.save(importStatus);
                }
            });
        } catch (Exception e) {
            importStatus.setStatus(Status.FAILED);
            importStatus.setErrorMessage(e.getMessage());
            importStatus.setEndDate(LocalDateTime.now());
            importStatusRepository.save(importStatus);
            throw new RuntimeException(e.getMessage());
        }

        return importStatus.getId();
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<Person> processFile(MultipartFile file, ImportStatus importStatus) throws Exception {


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            List<CSVRecord> recordList = new ArrayList<>();
            records.forEach(recordList::add);

            ForkJoinTask<List<Person>> task = forkJoinPool.submit(
                    batchProcessingServiceFactory.createBatchProcessingService(recordList, importStatus, 0, recordList.size())
            );
            List<Person> personList = task.join();

            if (BatchProcessingService.hasErrorOccurred.get()) {
                forkJoinPool.shutdownNow();
                throw new RuntimeException("Import canceled due to error");
            }

            return personList;
        }


    }

    public ImportStatus getImportStatus(Long importId) {
        return importStatusRepository.findById(importId)
                .orElseThrow(() -> new IllegalArgumentException("Import not found"));
    }
}
