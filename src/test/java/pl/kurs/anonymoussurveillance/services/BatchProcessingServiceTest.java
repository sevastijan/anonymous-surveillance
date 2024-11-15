//package pl.kurs.anonymoussurveillance.services;
//
//import org.apache.commons.csv.CSVRecord;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import pl.kurs.anonymoussurveillance.models.*;
//import pl.kurs.anonymoussurveillance.repositories.*;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class BatchProcessingServiceTest {
//
//    @Mock
//    private PersonRepository personRepository;
//
//    @Mock
//    private PersonTypeRepository personTypeRepository;
//
//    @Mock
//    private PersonAttributeRepository personAttributeRepository;
//
//    @Mock
//    private EmploymentRepository employmentRepository;
//
//    @Mock
//    private ImportStatusRepository importStatusRepository;
//
//    private ImportStatus importStatus;
//
//    private Map<String, PersonType> personTypeCache;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        importStatus = new ImportStatus();
//
//        PersonType employeeType = new PersonType();
//        employeeType.setName("employee");
//
//        RequiredAttribute employeeAttribute1 = new RequiredAttribute();
//        employeeAttribute1.setName("employeeAttribute1");
//        employeeAttribute1.setAttributeType(AttributeType.STRING);
//
//        RequiredAttribute employeeAttribute2 = new RequiredAttribute();
//        employeeAttribute2.setName("employeeAttribute2");
//        employeeAttribute2.setAttributeType(AttributeType.INTEGER);
//
//        employeeType.setRequiredAttributes(Arrays.asList(employeeAttribute1, employeeAttribute2));
//
//        PersonType customerType = new PersonType();
//        customerType.setName("customer");
//
//        RequiredAttribute customerAttribute = new RequiredAttribute();
//        customerAttribute.setName("customerAttribute");
//        customerAttribute.setAttributeType(AttributeType.DATE);
//
//        customerType.setRequiredAttributes(Collections.singletonList(customerAttribute));
//
//        when(personTypeRepository.findAll()).thenReturn(Arrays.asList(employeeType, customerType));
//
//        personTypeCache = new HashMap<>();
//        personTypeCache.put("employee", employeeType);
//        personTypeCache.put("customer", customerType);
//
//        BatchProcessingService.hasErrorOccurred.set(false);
//    }
//
//
//}
