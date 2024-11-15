package pl.kurs.anonymoussurveillance.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportService {
    private final ImportStatusRepository importStatusRepository;
    private final PersonTypeRepository personTypeRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(64);
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    Future<?> currentImportTask;
    private final Object importLock = new Object();

    @Transactional
    public Long importFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }

        synchronized (importLock) {
            if (currentImportTask != null && !currentImportTask.isDone()) {
                throw new IllegalStateException("Import already in progress");
            }

            ImportStatus importStatus = new ImportStatus();
            importStatus.setStatus(Status.PENDING);
            importStatus.setCreatedDate(LocalDateTime.now());
            importStatusRepository.save(importStatus);

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                currentImportTask = executorService.submit(() -> {
                    try {
                        processFile(reader, importStatus);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                return importStatus.getId();
            } catch (Exception e) {
                importStatus.setStatus(Status.FAILED);
                importStatus.setErrorMessage(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processFile(BufferedReader file, ImportStatus importStatus) throws Exception {

        BatchProcessingService batchProcessor = new BatchProcessingService(transactionManager, jdbcTemplate, personTypeRepository);

        try {
            importStatus.setStatus(Status.IN_PROGRESS);
            importStatus.setStartDate(LocalDateTime.now());
            importStatusRepository.save(importStatus);

            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(file);
            Iterator<CSVRecord> iterator = parser.iterator();

            List<CSVRecord> recordList = new ArrayList<>();

            parser.forEach(recordList::add);

            int totalProcessed = 0;
            int batchSize = 2000;

            for (int i = 0; i < recordList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, recordList.size());
                List<CSVRecord> batch = recordList.subList(i, end);

                List<Person> processedBatch = batchProcessor.processBatch(batch);
                totalProcessed += processedBatch.size();

                importStatus.setProcessedRows(totalProcessed);
                importStatusRepository.save(importStatus);
            }
            importStatus.setStatus(Status.COMPLETED);
        } catch (Exception e) {
            importStatus.setStatus(Status.FAILED);
            importStatus.setErrorMessage(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            importStatus.setEndDate(LocalDateTime.now());

            if (importStatus.getStartDate() != null) {
                double timeDifference = java.time.Duration.between(importStatus.getStartDate(), importStatus.getEndDate()).toMillis() / 1000.0;
                double rowsPerSecond = importStatus.getProcessedRows() / timeDifference;
                importStatus.setRowsPerSecond(rowsPerSecond);
            }

            importStatusRepository.save(importStatus);
        }
    }

    public ImportStatus getImportStatus(Long importId) {
        return importStatusRepository.findById(importId)
                .orElseThrow(() -> new IllegalArgumentException("Import not found"));
    }
}
