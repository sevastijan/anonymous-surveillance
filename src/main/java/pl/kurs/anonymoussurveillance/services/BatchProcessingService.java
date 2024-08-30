package pl.kurs.anonymoussurveillance.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import pl.kurs.anonymoussurveillance.factories.BatchProcessingServiceFactory;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BatchProcessingService extends RecursiveTask<List<Person>> {
    private final int THRESHOLD = 20000;
    private final List<CSVRecord> records;
    private final ImportStatus importStatus;
    private final int start;
    private final int end;
    private final PersonRepository personRepository;
    private final PersonAttributeRepository personAttributeRepository;
    private final EmploymentRepository employmentRepository;
    private final BatchProcessingServiceFactory batchProcessingServiceFactory;
    private Map<String, PersonType> personTypeCache;


    public BatchProcessingService(List<CSVRecord> records, ImportStatus importStatus, int start, int end, PersonRepository personRepository, PersonTypeRepository personTypeRepository, PersonAttributeRepository personAttributeRepository, EmploymentRepository employmentRepository, ImportStatusRepository importStatusRepository, BatchProcessingServiceFactory batchProcessingServiceFactory) {
        this.records = records;
        this.importStatus = importStatus;
        this.start = start;
        this.end = end;
        this.personRepository = personRepository;
        this.personAttributeRepository = personAttributeRepository;
        this.employmentRepository = employmentRepository;
        this.batchProcessingServiceFactory = batchProcessingServiceFactory;
        this.personTypeCache = personTypeRepository.findAll().stream()
                .collect(Collectors.toMap(PersonType::getName, Function.identity()));

    }

    @Override
    protected List<Person> compute() {
        if (end - start <= THRESHOLD) {
            return processBatch(records.subList(start, end));
        } else {
            int mid = (start + end) / 2;
            BatchProcessingService task1 = batchProcessingServiceFactory.createBatchProcessingService(records, importStatus, start, mid);
            BatchProcessingService task2 = batchProcessingServiceFactory.createBatchProcessingService(records, importStatus, mid, end);
            invokeAll(task1, task2);
            List<Person> result = new ArrayList<>();
            result.addAll(task1.join());
            result.addAll(task2.join());

            return result;
        }
    }

    public List<Person> processBatch(List<CSVRecord> batch) {
        List<Person> personList = new ArrayList<>(batch.size());
        List<PersonAttribute> personAttributes = new ArrayList<>();
        List<Employment> employmentList = new ArrayList<>();

        for (CSVRecord record : batch) {
            String type = record.get("type");
            PersonType personType = personTypeCache.get(type);

            Person person = new Person();
            person.setPersonType(personType);

            if (type.equalsIgnoreCase("employee")) {
                int employmentCount = 1;
                while (true) {
                    String employmentStartDateKey = "employmentStartDate-" + employmentCount;
                    if (!record.isMapped(employmentStartDateKey) || record.get(employmentStartDateKey).isEmpty()) {
                        break;
                    }

                    Employment employment = new Employment();
                    employment.setStartDate(LocalDate.parse(record.get(employmentStartDateKey)));
                    employment.setEndDate(LocalDate.parse(record.get("employmentEndDate-" + employmentCount)));
                    employment.setCompanyName(record.get("companyName-" + employmentCount));
                    employment.setRole(record.get("role-" + employmentCount));
                    employment.setSalary(new BigDecimal(record.get("salary-" + employmentCount)));
                    employment.setPerson(person);

                    employmentList.add(employment);
                    employmentCount++;
                }
            }

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
        employmentRepository.saveAll(employmentList);

        //TODO: add importStatus update

        return personList;
    }
}
