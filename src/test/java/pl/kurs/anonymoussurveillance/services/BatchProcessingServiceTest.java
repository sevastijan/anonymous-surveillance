package pl.kurs.anonymoussurveillance.services;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import pl.kurs.anonymoussurveillance.factories.BatchProcessingServiceFactory;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BatchProcessingServiceTest {

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

    @Mock
    private BatchProcessingServiceFactory batchProcessingServiceFactory;

    private ImportStatus importStatus;

    private Map<String, PersonType> personTypeCache;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        importStatus = new ImportStatus();
        
        PersonType employeeType = new PersonType();
        employeeType.setName("employee");

        RequiredAttribute employeeAttribute1 = new RequiredAttribute();
        employeeAttribute1.setName("employeeAttribute1");
        employeeAttribute1.setAttributeType(AttributeType.STRING);

        RequiredAttribute employeeAttribute2 = new RequiredAttribute();
        employeeAttribute2.setName("employeeAttribute2");
        employeeAttribute2.setAttributeType(AttributeType.INTEGER);

        employeeType.setRequiredAttributes(Arrays.asList(employeeAttribute1, employeeAttribute2));

        PersonType customerType = new PersonType();
        customerType.setName("customer");

        RequiredAttribute customerAttribute = new RequiredAttribute();
        customerAttribute.setName("customerAttribute");
        customerAttribute.setAttributeType(AttributeType.DATE);

        customerType.setRequiredAttributes(Collections.singletonList(customerAttribute));

        when(personTypeRepository.findAll()).thenReturn(Arrays.asList(employeeType, customerType));
        
        personTypeCache = new HashMap<>();
        personTypeCache.put("employee", employeeType);
        personTypeCache.put("customer", customerType);

        when(batchProcessingServiceFactory.createBatchProcessingService(anyList(), any(), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    List<CSVRecord> records = invocation.getArgument(0);
                    ImportStatus status = invocation.getArgument(1);
                    int start = invocation.getArgument(2);
                    int end = invocation.getArgument(3);
                    return new BatchProcessingService(
                            records,
                            status,
                            start,
                            end,
                            personRepository,
                            personTypeRepository,
                            personAttributeRepository,
                            employmentRepository,
                            importStatusRepository,
                            batchProcessingServiceFactory
                    );
                });

        BatchProcessingService.hasErrorOccurred.set(false);
    }

    @Test
    public void shouldProcessValidEmployeeRecord() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("employee");
        when(record.isMapped("employeeAttribute1")).thenReturn(true);
        when(record.isMapped("employeeAttribute2")).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2020-01-01");
        when(record.get("employmentEndDate-1")).thenReturn("2020-12-31");
        when(record.get("companyName-1")).thenReturn("Company A");
        when(record.get("role-1")).thenReturn("Developer");
        when(record.get("salary-1")).thenReturn("5000");
        when(record.get("employeeAttribute1")).thenReturn("Attribute Value");
        when(record.get("employeeAttribute2")).thenReturn("42");
        when(record.get("pesel")).thenReturn("02310186193");

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        List<Person> result = batchProcessingService.compute();
        when(record.get("pesel")).thenReturn("02310186193");
        assertEquals(1, result.size());
    }


    @Test
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("employee");
        when(record.isMapped(anyString())).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2020-12-31");
        when(record.get("employmentEndDate-1")).thenReturn("2020-01-01");
        when(record.get("companyName-1")).thenReturn("Company A");
        when(record.get("role-1")).thenReturn("Developer");
        when(record.get("salary-1")).thenReturn("5000");
        when(record.get("employeeAttribute1")).thenReturn("Attribute Value");
        when(record.get("employeeAttribute2")).thenReturn("42");
        when(record.get("pesel")).thenReturn("02310186193");

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        assertThrows(IllegalArgumentException.class, batchProcessingService::compute);
    }

    @Test
    public void shouldProcessValidCustomerRecord() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("customer");
        when(record.isMapped(anyString())).thenReturn(true);
        when(record.get("customerAttribute")).thenReturn("2020-01-01");
        when(record.get("pesel")).thenReturn("02310186193");

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        List<Person> result = batchProcessingService.compute();

        assertEquals(1, result.size());
    }

    @Test
    public void shouldSplitTasksWhenThresholdExceeded() {
        List<CSVRecord> records = new ArrayList<>();

        for (int i = 0; i < 25000; i++) {
            CSVRecord record = mock(CSVRecord.class);
            when(record.get("type")).thenReturn("customer");
            when(record.isMapped(anyString())).thenReturn(true);
            when(record.get("customerAttribute")).thenReturn("2020-01-01");
            when(record.get("pesel")).thenReturn("02310186193");
            records.add(record);
        }

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                records.size(),
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        batchProcessingService.compute();

        verify(batchProcessingServiceFactory, atLeastOnce()).createBatchProcessingService(anyList(), eq(importStatus), anyInt(), anyInt());
    }

    @Test
    public void shouldThrowExceptionForMissingRequiredAttribute() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("employee");
        when(record.isMapped("employeeAttribute1")).thenReturn(true);
        when(record.isMapped("employeeAttribute2")).thenReturn(true);

        when(record.get("employeeAttribute1")).thenReturn("Attribute Value");
        when(record.get("employeeAttribute2")).thenReturn(null);
        when(record.get("employmentStartDate-1")).thenReturn("2020-01-01");
        when(record.get("employmentEndDate-1")).thenReturn("2020-12-31");
        when(record.get("companyName-1")).thenReturn("Company A");
        when(record.get("role-1")).thenReturn("Developer");
        when(record.get("salary-1")).thenReturn("5000");

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        assertThrows(IllegalArgumentException.class, batchProcessingService::compute);
    }


    @Test
    public void shouldValidateAttributeType() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("employee");
        when(record.isMapped("employeeAttribute1")).thenReturn(true);
        when(record.isMapped("employeeAttribute2")).thenReturn(true);
        when(record.isMapped("employmentStartDate-1")).thenReturn(false); // To prevent processing
        when(record.get("employmentStartDate-1")).thenReturn("2020-01-01");
        when(record.get("employmentEndDate-1")).thenReturn("2020-12-31");
        when(record.get("companyName-1")).thenReturn("Company A");
        when(record.get("role-1")).thenReturn("Developer");
        when(record.get("salary-1")).thenReturn("5000");
        when(record.get("employeeAttribute1")).thenReturn("Attribute Value");
        when(record.get("employeeAttribute2")).thenReturn("Invalid Integer");
        when(record.get("pesel")).thenReturn("02310186193");

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, batchProcessingService::compute);

        assertEquals("Invalid value 'Invalid Integer' for attribute 'employeeAttribute2' of type INTEGER", exception.getMessage());
    }


    @Test
    public void shouldThrowExceptionForInvalidSalaryFormat() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("employee");
        when(record.isMapped(anyString())).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2020-01-01");
        when(record.get("employmentEndDate-1")).thenReturn("2020-12-31");
        when(record.get("companyName-1")).thenReturn("Company A");
        when(record.get("role-1")).thenReturn("Developer");
        when(record.get("salary-1")).thenReturn("Invalid Salary");
        when(record.get("employeeAttribute1")).thenReturn("Attribute Value");
        when(record.get("employeeAttribute2")).thenReturn("42");
        when(record.get("pesel")).thenReturn("02310186193");

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        assertThrows(IllegalArgumentException.class, batchProcessingService::compute);
    }

    @Test
    public void shouldProcessMultipleEmploymentsForEmployee() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("employee");
        when(record.isMapped(anyString())).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2020-01-01");
        when(record.get("employmentEndDate-1")).thenReturn("2020-12-31");
        when(record.get("companyName-1")).thenReturn("Company A");
        when(record.get("role-1")).thenReturn("Developer");
        when(record.get("salary-1")).thenReturn("5000");
        when(record.get("employmentStartDate-2")).thenReturn("2021-01-01");
        when(record.get("employmentEndDate-2")).thenReturn("2021-12-31");
        when(record.get("companyName-2")).thenReturn("Company B");
        when(record.get("role-2")).thenReturn("Senior Developer");
        when(record.get("salary-2")).thenReturn("7000");
        when(record.get("pesel")).thenReturn("02310186193");

        when(record.get("employmentStartDate-3")).thenReturn("");
        when(record.get("employeeAttribute1")).thenReturn("Attribute Value");
        when(record.get("employeeAttribute2")).thenReturn("42");

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );
        List<Person> result = batchProcessingService.compute();

        assertEquals(1, result.size());
        verify(employmentRepository).saveAll(anyList());
        ArgumentCaptor<List<Employment>> employmentCaptor = ArgumentCaptor.forClass(List.class);
        verify(employmentRepository).saveAll(employmentCaptor.capture());

        List<Employment> savedEmployments = employmentCaptor.getValue();
        assertEquals(2, savedEmployments.size());
    }

    @Test
    public void shouldHandleUnmappedAttributes() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("customer");
        when(record.isMapped("customerAttribute")).thenReturn(false);
        when(record.get("pesel")).thenReturn("02310186193");

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        assertThrows(IllegalArgumentException.class, batchProcessingService::compute);
    }

    @Test
    public void shouldThrowExceptionForUnknownPersonType() {
        CSVRecord record = mock(CSVRecord.class);

        when(record.get("type")).thenReturn("unknownType");
        when(record.isMapped(anyString())).thenReturn(true);

        List<CSVRecord> records = Collections.singletonList(record);

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                records,
                importStatus,
                0,
                1,
                personRepository,
                personTypeRepository,
                personAttributeRepository,
                employmentRepository,
                importStatusRepository,
                batchProcessingServiceFactory
        );

        assertThrows(IllegalArgumentException.class, batchProcessingService::compute);
    }
}
