package pl.kurs.anonymoussurveillance.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import pl.kurs.anonymoussurveillance.factories.BatchProcessingServiceFactory;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private static final AtomicBoolean hasErrorOccurred = new AtomicBoolean(false);


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

                    LocalDate startDate;
                    try {
                        startDate = LocalDate.parse(record.get(employmentStartDateKey));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid employment start date format");
                    }

                    String employmentEndDateKey = "employmentEndDate-" + employmentCount;
                    LocalDate endDate;
                    try {
                        endDate = LocalDate.parse(record.get(employmentEndDateKey));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid employment end date format");
                    }

                    if (endDate.isBefore(startDate)) {
                        throw new IllegalArgumentException("Employment end date cannot be before start date");
                    }

                    String companyName = record.get("companyName-" + employmentCount);
                    if (companyName == null || companyName.trim().isEmpty()) {
                        throw new IllegalArgumentException("Company name is required");
                    }

                    String role = record.get("role-" + employmentCount);
                    if (role == null || role.trim().isEmpty()) {
                        throw new IllegalArgumentException("Role is required");
                    }

                    BigDecimal salary;
                    try {
                        salary = new BigDecimal(record.get("salary-" + employmentCount));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid salary format");
                    }

                    Employment employment = new Employment();
                    employment.setStartDate(startDate);
                    employment.setEndDate(endDate);
                    employment.setCompanyName(companyName);
                    employment.setRole(role);
                    employment.setSalary(salary);
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

                    if(!validateAttributeType(attributeValue, typeAttribute.getAttributeType())) {
                        throw new IllegalArgumentException("Invalid value '" + attributeValue + "' for attribute '" + attributeName + "' of type " + typeAttribute.getAttributeType());
                    }

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

        return personList;
    }

    private boolean validateAttributeType(String attributeValue, AttributeType attributeType) {
        switch (attributeType) {
            case STRING:
                return true;
            case INTEGER:
                try {
                    Integer.parseInt(attributeValue);
                } catch (NumberFormatException e) {
                    return false;
                }
            case DOUBLE:
                try {
                    Double.parseDouble(attributeValue);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case BIG_DECIMAL:
                try {
                    new BigDecimal(attributeValue);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case DATE:
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime.parse(attributeValue, formatter);
                    return true;
                } catch (DateTimeParseException e) {
                    return false;
                }
            default:
                return false;
        }
    }

}
