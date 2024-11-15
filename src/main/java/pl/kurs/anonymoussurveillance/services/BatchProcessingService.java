package pl.kurs.anonymoussurveillance.services;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BatchProcessingService {
    private Map<String, PersonType> personTypeCache;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;


    public BatchProcessingService(PlatformTransactionManager transactionManager, JdbcTemplate jbdcTemplate, PersonTypeRepository personTypeRepository) {
        this.transactionManager = transactionManager;
        this.jdbcTemplate = jbdcTemplate;

        this.personTypeCache = personTypeRepository.findAll().stream()
                .collect(Collectors.toMap(PersonType::getName, Function.identity()));

    }

    public List<Person> processBatch(List<CSVRecord> batch) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {
            try {
                List<Map<String, Object>> personDataList = new ArrayList<>();
                List<Map<String, Object>> attributeDataList = new ArrayList<>();
                List<Map<String, Object>> employmentDataList = new ArrayList<>();

                for (CSVRecord record : batch) {
                    String type = record.get("type");
                    String pesel = record.get("pesel");
                    PersonType personType = personTypeCache.get(type);

                    if (personType == null) {
                        throw new IllegalArgumentException("Unknown person type: " + type);
                    }
                    if (pesel == null) {
                        throw new IllegalArgumentException("PESEL is required");
                    }

                    Map<String, Object> personData = new HashMap<>();
                    personData.put("pesel", pesel);
                    personData.put("person_type_id", personType.getId());
                    personData.put("version", 0L);
                    personDataList.add(personData);

                    int personIndex = personDataList.size() - 1;

                    for (RequiredAttribute typeAttribute : personType.getRequiredAttributes()) {
                        String attributeName = typeAttribute.getName();
                        if (!record.isMapped(attributeName)) {
                            throw new IllegalArgumentException("Missing required attribute '" + attributeName + "'");
                        }

                        String attributeValue = record.get(attributeName);

                        if (attributeValue == null || attributeValue.isEmpty()) {
                            throw new IllegalArgumentException("Attribute '" + attributeName + "' cannot be null or empty.");
                        }

                        if (!validateAttributeType(attributeValue, typeAttribute.getAttributeType())) {
                            throw new IllegalArgumentException("Invalid value '" + attributeValue + "' for attribute '" + attributeName + "' of type " + typeAttribute.getAttributeType());
                        }

                        Map<String, Object> attributeData = new HashMap<>();
                        attributeData.put("name", attributeName);
                        attributeData.put("type", typeAttribute.getAttributeType().name());
                        attributeData.put("value", attributeValue);
                        attributeData.put("person_index", personIndex);
                        attributeDataList.add(attributeData);
                    }

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
                                throw new IllegalArgumentException("Invalid salary format: " + record.get("salary-" + employmentCount));
                            }

                            Map<String, Object> employmentData = new HashMap<>();
                            employmentData.put("start_date", startDate);
                            employmentData.put("end_date", endDate);
                            employmentData.put("company_name", companyName);
                            employmentData.put("role", role);
                            employmentData.put("salary", salary);
                            employmentData.put("person_index", personIndex);
                            employmentDataList.add(employmentData);

                            employmentCount++;
                        }
                    }
                }

                SimpleJdbcInsert personInsert = new SimpleJdbcInsert(jdbcTemplate)
                        .withTableName("person_list")
                        .usingGeneratedKeyColumns("id")
                        .usingColumns("pesel", "person_type_id", "version");

                List<Long> personIds = new ArrayList<>();

                for (Map<String, Object> personData : personDataList) {
                    Number generatedId = personInsert.executeAndReturnKey(personData);
                    personIds.add(generatedId.longValue());
                }

                for (Map<String, Object> attributeData : attributeDataList) {
                    int index = (int) attributeData.get("person_index");
                    attributeData.put("person_id", personIds.get(index));
                    attributeData.remove("person_index");
                }

                for (Map<String, Object> employmentData : employmentDataList) {
                    int index = (int) employmentData.get("person_index");
                    employmentData.put("person_id", personIds.get(index));
                    employmentData.remove("person_index");
                }

                String attrSql = "INSERT INTO person_attributes (name, type, value, person_id) VALUES (?, ?, ?, ?)";
                List<Object[]> attrParams = new ArrayList<>();
                for (Map<String, Object> attributeData : attributeDataList) {
                    Object[] params = new Object[]{
                            attributeData.get("name"),
                            attributeData.get("type"),
                            attributeData.get("value"),
                            attributeData.get("person_id")
                    };
                    attrParams.add(params);
                }
                jdbcTemplate.batchUpdate(attrSql, attrParams);

                if (!employmentDataList.isEmpty()) {
                    String empSql = "INSERT INTO employment_list (start_date, end_date, company_name, role, salary, person_id) VALUES (?, ?, ?, ?, ?, ?)";
                    List<Object[]> empParams = new ArrayList<>();
                    for (Map<String, Object> employmentData : employmentDataList) {
                        Object[] params = new Object[]{
                                employmentData.get("start_date"),
                                employmentData.get("end_date"),
                                employmentData.get("company_name"),
                                employmentData.get("role"),
                                employmentData.get("salary"),
                                employmentData.get("person_id")
                        };
                        empParams.add(params);
                    }
                    jdbcTemplate.batchUpdate(empSql, empParams);
                }

                List<Person> personList = new ArrayList<>();
                for (int i = 0; i < personDataList.size(); i++) {
                    Map<String, Object> personData = personDataList.get(i);
                    Long personId = personIds.get(i);

                    Person person = new Person();
                    person.setId(personId);
                    person.setPesel((String) personData.get("pesel"));
                    Long personTypeId = (Long) personData.get("person_type_id");
                    person.setPersonType(personTypeCache.values().stream()
                            .filter(pt -> pt.getId().equals(personTypeId))
                            .findFirst()
                            .orElse(null));
                    person.setVersion(personData.get("version") != null ? (Long) personData.get("version") : 0L);
                    personList.add(person);
                }

                return personList;

            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    private boolean validateAttributeType(String attributeValue, AttributeType attributeType) {
        if (attributeValue == null || attributeValue.isEmpty()) {
            return false;
        }

        switch (attributeType) {
            case STRING:
                return true;
            case INTEGER:
                try {
                    Integer.parseInt(attributeValue);
                    return true;
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
                    LocalDate.parse(attributeValue, formatter);
                    return true;
                } catch (DateTimeParseException e) {
                    return false;
                }
            default:
                return false;
        }
    }
}