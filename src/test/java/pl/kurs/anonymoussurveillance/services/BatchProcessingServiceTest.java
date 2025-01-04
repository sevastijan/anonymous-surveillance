package pl.kurs.anonymoussurveillance.services;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
public class BatchProcessingServiceTest {

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PersonTypeRepository personTypeRepository;

    @InjectMocks
    private BatchProcessingService batchProcessingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        RequiredAttribute attr = new RequiredAttribute("age", AttributeType.INTEGER);
        employeeType.setRequiredAttributes(List.of(attr));

        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));

        batchProcessingService = new BatchProcessingService(transactionManager, jdbcTemplate, personTypeRepository);
    }

    @Test
    public void shouldProcessValidBatchSuccessfully() throws SQLException {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        ResultSet columnsResultSet = mock(ResultSet.class);
        ResultSet generatedKeysResultSet = mock(ResultSet.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);


        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getColumns(any(), any(), eq("person_list"), any())).thenReturn(columnsResultSet);
        when(columnsResultSet.next()).thenReturn(false);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenReturn(1L);
        when(jdbcTemplate.execute(any(PreparedStatementCreator.class), any(PreparedStatementCallback.class))).thenReturn(1L);

        when(metaData.supportsGetGeneratedKeys()).thenReturn(true);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeysResultSet);
        when(generatedKeysResultSet.next()).thenReturn(true);
        when(generatedKeysResultSet.getLong(1)).thenReturn(1L);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        keyHolder.getKeyList().add(new HashMap<String, Object>() {{ put("id", 1L); }});

        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder holder = invocation.getArgument(1);
                    holder.getKeyList().add(Collections.singletonMap("id", 1L));
                    return 1;
                });

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        employeeType.setRequiredAttributes(List.of(
                new RequiredAttribute("age", AttributeType.INTEGER)
        ));

        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("employee");
        when(record.get("pesel")).thenReturn("12345678901");
        when(record.isMapped("age")).thenReturn(true);
        when(record.get("age")).thenReturn("30");
        when(record.isMapped("employmentStartDate-1")).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2024-01-01");
        when(record.isMapped("employmentEndDate-1")).thenReturn(true);
        when(record.get("employmentEndDate-1")).thenReturn("2024-12-31");
        when(record.get("companyName-1")).thenReturn("Test Company");
        when(record.get("role-1")).thenReturn("Developer");
        when(record.get("salary-1")).thenReturn("10000");
        when(record.isMapped("employmentStartDate-2")).thenReturn(false);

        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[]{1});

        List<Person> result = batchProcessingService.processBatch(List.of(record));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("12345678901", result.get(0).getPesel());

    }

    @Test
    public void shouldThrowExceptionWhenPersonTypeIsUnknown() {
        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("unknown");
        when(record.get("pesel")).thenReturn("12345678901");

        List<CSVRecord> batch = List.of(record);

        assertThrows(IllegalArgumentException.class, () -> batchProcessingService.processBatch(batch),
                "Unknown person type: unknown");
    }

    @Test
    public void shouldThrowExceptionWhenRequiredAttributeIsMissing() {
        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("employee");
        when(record.get("pesel")).thenReturn("12345678901");
        when(record.isMapped("age")).thenReturn(false);

        List<CSVRecord> batch = List.of(record);

        assertThrows(IllegalArgumentException.class, () -> batchProcessingService.processBatch(batch),
                "Missing required attribute 'age'");
    }

    @Test
    public void shouldThrowExceptionWhenEmploymentDatesAreInvalid() {
        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("employee");
        when(record.get("pesel")).thenReturn("12345678901");
        when(record.isMapped("age")).thenReturn(true);
        when(record.get("age")).thenReturn("30");
        when(record.isMapped("employmentStartDate-1")).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2024-06-01");
        when(record.get("employmentEndDate-1")).thenReturn("2024-05-01");

        List<CSVRecord> batch = List.of(record);

        assertThrows(IllegalArgumentException.class, () -> batchProcessingService.processBatch(batch),
                "Employment end date cannot be before start date");
    }

    @Test
    public void shouldThrowExceptionWhenAttributeValueIsInvalid() {
        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("employee");
        when(record.get("pesel")).thenReturn("12345678901");
        when(record.isMapped("age")).thenReturn(true);
        when(record.get("age")).thenReturn("notANumber");

        List<CSVRecord> batch = List.of(record);

        assertThrows(IllegalArgumentException.class, () -> batchProcessingService.processBatch(batch),
                "Invalid value 'notANumber' for attribute 'age' of type INTEGER");
    }

    @Test
    public void shouldThrowExceptionWhenPeselIsNull() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        employeeType.setRequiredAttributes(List.of(
                new RequiredAttribute("age", AttributeType.INTEGER)
        ));
        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("employee");
        when(record.get("pesel")).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(record))
        );
    }

    @Test
    public void shouldThrowExceptionWhenRequiredAttributeIsNullOrEmpty() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        employeeType.setRequiredAttributes(List.of(
                new RequiredAttribute("age", AttributeType.INTEGER)
        ));
        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord recordWithNullAge = mock(CSVRecord.class);
        when(recordWithNullAge.get("type")).thenReturn("employee");
        when(recordWithNullAge.get("pesel")).thenReturn("12345678901");
        when(recordWithNullAge.isMapped("age")).thenReturn(true);
        when(recordWithNullAge.get("age")).thenReturn(null);

        IllegalArgumentException nullException = assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithNullAge))
        );
        assertEquals("Attribute 'age' cannot be null or empty.", nullException.getMessage());

        CSVRecord recordWithEmptyAge = mock(CSVRecord.class);
        when(recordWithEmptyAge.get("type")).thenReturn("employee");
        when(recordWithEmptyAge.get("pesel")).thenReturn("12345678901");
        when(recordWithEmptyAge.isMapped("age")).thenReturn(true);
        when(recordWithEmptyAge.get("age")).thenReturn("");

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithEmptyAge))
        );
    }

    @Test
    public void shouldThrowExceptionWhenEmploymentStartDateHasInvalidFormat() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        employeeType.setRequiredAttributes(List.of(
                new RequiredAttribute("age", AttributeType.INTEGER)
        ));
        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("employee");
        when(record.get("pesel")).thenReturn("12345678901");
        when(record.isMapped("age")).thenReturn(true);
        when(record.get("age")).thenReturn("30");
        when(record.isMapped("employmentStartDate-1")).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2024.01.01");

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(record))
        );
    }

    @Test
    public void shouldThrowExceptionWhenEmploymentEndDateHasInvalidFormat() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        employeeType.setRequiredAttributes(List.of(
                new RequiredAttribute("age", AttributeType.INTEGER)
        ));
        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("employee");
        when(record.get("pesel")).thenReturn("12345678901");
        when(record.isMapped("age")).thenReturn(true);
        when(record.get("age")).thenReturn("30");
        when(record.isMapped("employmentStartDate-1")).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2024-01-01");
        when(record.isMapped("employmentEndDate-1")).thenReturn(true);
        when(record.get("employmentEndDate-1")).thenReturn("2024.12.31");

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(record))
        );
    }

    @Test
    public void shouldThrowExceptionWhenCompanyNameIsNullOrEmpty() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        employeeType.setRequiredAttributes(List.of(
                new RequiredAttribute("age", AttributeType.INTEGER)
        ));
        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord recordWithNullCompany = mock(CSVRecord.class);
        when(recordWithNullCompany.get("type")).thenReturn("employee");
        when(recordWithNullCompany.get("pesel")).thenReturn("12345678901");
        when(recordWithNullCompany.isMapped("age")).thenReturn(true);
        when(recordWithNullCompany.get("age")).thenReturn("30");
        when(recordWithNullCompany.isMapped("employmentStartDate-1")).thenReturn(true);
        when(recordWithNullCompany.get("employmentStartDate-1")).thenReturn("2024-01-01");
        when(recordWithNullCompany.isMapped("employmentEndDate-1")).thenReturn(true);
        when(recordWithNullCompany.get("employmentEndDate-1")).thenReturn("2024-12-31");
        when(recordWithNullCompany.get("companyName-1")).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithNullCompany))
        );

        CSVRecord recordWithEmptyCompany = mock(CSVRecord.class);
        when(recordWithEmptyCompany.get("type")).thenReturn("employee");
        when(recordWithEmptyCompany.get("pesel")).thenReturn("12345678901");
        when(recordWithEmptyCompany.isMapped("age")).thenReturn(true);
        when(recordWithEmptyCompany.get("age")).thenReturn("30");
        when(recordWithEmptyCompany.isMapped("employmentStartDate-1")).thenReturn(true);
        when(recordWithEmptyCompany.get("employmentStartDate-1")).thenReturn("2024-01-01");
        when(recordWithEmptyCompany.isMapped("employmentEndDate-1")).thenReturn(true);
        when(recordWithEmptyCompany.get("employmentEndDate-1")).thenReturn("2024-12-31");
        when(recordWithEmptyCompany.get("companyName-1")).thenReturn("   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithEmptyCompany))
        );
    }

    @Test
    public void shouldThrowExceptionWhenRoleIsNullOrEmpty() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        employeeType.setRequiredAttributes(List.of(
                new RequiredAttribute("age", AttributeType.INTEGER)
        ));
        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord recordWithNullRole = mock(CSVRecord.class);
        when(recordWithNullRole.get("type")).thenReturn("employee");
        when(recordWithNullRole.get("pesel")).thenReturn("12345678901");
        when(recordWithNullRole.isMapped("age")).thenReturn(true);
        when(recordWithNullRole.get("age")).thenReturn("30");
        when(recordWithNullRole.isMapped("employmentStartDate-1")).thenReturn(true);
        when(recordWithNullRole.get("employmentStartDate-1")).thenReturn("2024-01-01");
        when(recordWithNullRole.isMapped("employmentEndDate-1")).thenReturn(true);
        when(recordWithNullRole.get("employmentEndDate-1")).thenReturn("2024-12-31");
        when(recordWithNullRole.get("companyName-1")).thenReturn("Test Company");
        when(recordWithNullRole.get("role-1")).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithNullRole))
        );

        CSVRecord recordWithEmptyRole = mock(CSVRecord.class);
        when(recordWithEmptyRole.get("type")).thenReturn("employee");
        when(recordWithEmptyRole.get("pesel")).thenReturn("12345678901");
        when(recordWithEmptyRole.isMapped("age")).thenReturn(true);
        when(recordWithEmptyRole.get("age")).thenReturn("30");
        when(recordWithEmptyRole.isMapped("employmentStartDate-1")).thenReturn(true);
        when(recordWithEmptyRole.get("employmentStartDate-1")).thenReturn("2024-01-01");
        when(recordWithEmptyRole.isMapped("employmentEndDate-1")).thenReturn(true);
        when(recordWithEmptyRole.get("employmentEndDate-1")).thenReturn("2024-12-31");
        when(recordWithEmptyRole.get("companyName-1")).thenReturn("Test Company");
        when(recordWithEmptyRole.get("role-1")).thenReturn("   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithEmptyRole))
        );
    }

    @Test
    public void shouldThrowExceptionWhenSalaryHasInvalidFormat() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);

        PersonType employeeType = new PersonType();
        employeeType.setId(1L);
        employeeType.setName("employee");
        employeeType.setRequiredAttributes(List.of(
                new RequiredAttribute("age", AttributeType.INTEGER)
        ));
        when(personTypeRepository.findAll()).thenReturn(List.of(employeeType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord record = mock(CSVRecord.class);
        when(record.get("type")).thenReturn("employee");
        when(record.get("pesel")).thenReturn("12345678901");
        when(record.isMapped("age")).thenReturn(true);
        when(record.get("age")).thenReturn("30");
        when(record.isMapped("employmentStartDate-1")).thenReturn(true);
        when(record.get("employmentStartDate-1")).thenReturn("2024-01-01");
        when(record.isMapped("employmentEndDate-1")).thenReturn(true);
        when(record.get("employmentEndDate-1")).thenReturn("2024-12-31");
        when(record.get("companyName-1")).thenReturn("Test Company");
        when(record.get("role-1")).thenReturn("Developer");
        when(record.get("salary-1")).thenReturn("invalid_salary");

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(record))
        );
    }

    @Test
    public void shouldValidateAttributeTypesInBatch() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PersonTypeRepository personTypeRepository = mock(PersonTypeRepository.class);

        PersonType personType = new PersonType();
        personType.setId(1L);
        personType.setName("employee");
        personType.setRequiredAttributes(List.of(
                new RequiredAttribute("salary", AttributeType.DOUBLE),
                new RequiredAttribute("bonus", AttributeType.BIG_DECIMAL),
                new RequiredAttribute("startDate", AttributeType.DATE)
        ));

        when(personTypeRepository.findAll()).thenReturn(List.of(personType));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        BatchProcessingService batchProcessingService = new BatchProcessingService(
                transactionManager,
                jdbcTemplate,
                personTypeRepository
        );

        CSVRecord recordWithInvalidDouble = mock(CSVRecord.class);
        when(recordWithInvalidDouble.get("type")).thenReturn("employee");
        when(recordWithInvalidDouble.get("pesel")).thenReturn("12345678901");
        when(recordWithInvalidDouble.isMapped("salary")).thenReturn(true);
        when(recordWithInvalidDouble.get("salary")).thenReturn("not_a_double");

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithInvalidDouble))
        );


        CSVRecord recordWithInvalidBigDecimal = mock(CSVRecord.class);
        when(recordWithInvalidBigDecimal.get("type")).thenReturn("employee");
        when(recordWithInvalidBigDecimal.get("pesel")).thenReturn("12345678901");
        when(recordWithInvalidBigDecimal.isMapped("salary")).thenReturn(true);
        when(recordWithInvalidBigDecimal.get("salary")).thenReturn("123.45");
        when(recordWithInvalidBigDecimal.isMapped("bonus")).thenReturn(true);
        when(recordWithInvalidBigDecimal.get("bonus")).thenReturn("not_a_decimal");

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithInvalidBigDecimal))
        );

        CSVRecord recordWithInvalidDate = mock(CSVRecord.class);
        when(recordWithInvalidDate.get("type")).thenReturn("employee");
        when(recordWithInvalidDate.get("pesel")).thenReturn("12345678901");
        when(recordWithInvalidDate.isMapped("salary")).thenReturn(true);
        when(recordWithInvalidDate.get("salary")).thenReturn("123.45");
        when(recordWithInvalidDate.isMapped("bonus")).thenReturn(true);
        when(recordWithInvalidDate.get("bonus")).thenReturn("500.00");
        when(recordWithInvalidDate.isMapped("startDate")).thenReturn(true);
        when(recordWithInvalidDate.get("startDate")).thenReturn("2024.01.01");  // niepoprawny format daty

        assertThrows(
                IllegalArgumentException.class,
                () -> batchProcessingService.processBatch(List.of(recordWithInvalidDate))
        );
    }
}
