package pl.kurs.anonymoussurveillance.services;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.ImportStatusRepository;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;
import pl.kurs.anonymoussurveillance.repositories.PersonAttributeRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final ImportStatusRepository importStatusRepository;
    private final PersonRepository personRepository;
    private final PersonTypeRepository personTypeRepository;
    private final PersonAttributeRepository personAttributeRepository;
    private ExecutorService executorService;
    private Future<?> currentImportTask;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(8);
    }

    public Long importFile(MultipartFile file) {
        //TODO: make this method transactional
        if (currentImportTask != null && !currentImportTask.isDone()) {
            throw new IllegalStateException("An import is already in progress");
        }

        ImportStatus importStatus = new ImportStatus();
        importStatus.setStatus(Status.PENDING);
        importStatus.setCreatedDate(LocalDateTime.now());
        importStatusRepository.save(importStatus);

        currentImportTask = executorService.submit(() -> {
            try {
                importStatus.setStatus(Status.IN_PROGRESS);
                importStatus.setStartDate(LocalDateTime.now());
                importStatusRepository.save(importStatus);

                processFile(file, importStatus);

                importStatus.setStatus(Status.COMPLETED);
                importStatus.setEndDate(LocalDateTime.now());

                double timeDifference = java.time.Duration.between(importStatus.getStartDate(), importStatus.getEndDate()).toMillis() / 1000.0;
                double rowsPerSecond = importStatus.getProcessedRows() / timeDifference;
                importStatus.setRowsPerSecond(rowsPerSecond);

                importStatusRepository.save(importStatus);
            } catch (Exception e) {
                importStatus.setStatus(Status.FAILED);
                importStatus.setEndDate(LocalDateTime.now());
                importStatus.setErrorMessage(e.getMessage());

                importStatusRepository.save(importStatus);
                throw new RuntimeException(e.getMessage());
            }
        });

        return importStatus.getId();
    }

    @Transactional
    public List<Person> processFile(MultipartFile file, ImportStatus importStatus) throws Exception {
        List<Person> personList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            List<CSVRecord> recordList = new ArrayList<>();
            records.forEach(recordList::add);

            int batchSize = 5000;
            List<Future<List<Person>>> futures = new ArrayList<>();

            for (int i = 0; i < recordList.size(); i += batchSize) {
                int fromIndex = i;
                int toIndex = Math.min(i + batchSize, recordList.size());

                List<CSVRecord> batch = recordList.subList(fromIndex, toIndex);
                Future<List<Person>> future = executorService.submit(() -> processBatch(batch, importStatus));
                futures.add(future);
            }

            for (Future<List<Person>> future : futures) {
                List<Person> batchResult = future.get();
                personList.addAll(batchResult);
                importStatus.setProcessedRows(importStatus.getProcessedRows() + batchResult.size());
                importStatusRepository.save(importStatus);
            }
        }

        return personList;
    }
    public List<Person> processBatch(List<CSVRecord> batch, ImportStatus importStatus) {
        List<Person> personList = new ArrayList<>();
        List<PersonAttribute> personAttributes = new ArrayList<>();

        for (CSVRecord record : batch) {
            String type = record.get("type");
            Optional<PersonType> personTypeOpt = personTypeRepository.findByName(type);

            if (personTypeOpt.isEmpty()) {
                throw new IllegalArgumentException("Unknown type: " + type);
            }

            PersonType personType = personTypeOpt.get();

            if (personType.getRequiredAttributes() == null) {
                throw new IllegalStateException("PersonType attributes cannot be null");
            }
            personType.getRequiredAttributes().size();

            Person person = new Person();
            person.setPersonType(personType);

            personList.add(person);

            for (RequiredAttribute typeAttribute : personType.getRequiredAttributes()) {
                String attributeName = typeAttribute.getName();
                if (record.isMapped(attributeName)) {
                    String attributeValue = record.get(attributeName);

                    PersonAttribute personAttribute = new PersonAttribute();

                    personAttribute.setName(attributeName);
                    personAttribute.setType(typeAttribute.getAttributeType());
                    personAttribute.setValue(attributeValue);
                    personAttribute.setPerson(person);
                    personAttributes.add(personAttribute);
                }
            }
        }

        personRepository.saveAll(personList);
        personAttributeRepository.saveAll(personAttributes);

        //TODO: add importStatus update

        return personList;
    }

    public ImportStatus getImportStatus(Long importId) {
        return importStatusRepository.findById(importId)
                .orElseThrow(() -> new IllegalArgumentException("Import not found"));
    }
}
