package pl.kurs.anonymoussurveillance.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kurs.anonymoussurveillance.exceptions.EmploymentDateOverlapException;
import pl.kurs.anonymoussurveillance.exceptions.EmploymentNotFoundException;
import pl.kurs.anonymoussurveillance.exceptions.PersonNotFoundException;
import pl.kurs.anonymoussurveillance.models.Employment;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.repositories.EmploymentRepository;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmploymentServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @InjectMocks
    private EmploymentService employmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldCreateNewEmployment() {
        Long personId = 1L;
        Person person = new Person();
        Employment employment = new Employment(LocalDate.now().minusYears(1), LocalDate.now(), "Company A", "Developer", BigDecimal.valueOf(50000), person);
        person.setEmployment(Collections.emptySet());

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(employmentRepository.save(any(Employment.class))).thenReturn(employment);

        Person result = employmentService.createNewEmployment(personId, employment);

        assertNotNull(result);
        assertEquals(person, result);
    }

    @Test
    public void shouldThrowExceptionWhenPersonNotFound() {
        Long personId = 1L;
        Employment employment = new Employment();

        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> {
            employmentService.createNewEmployment(personId, employment);
        });
    }

    @Test
    public void shouldThrowExceptionWhenEmploymentDateOverlap() {
        Long personId = 1L;
        Person person = new Person();
        Employment existingEmployment = new Employment(1L, LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6), "Company B", "Manager", BigDecimal.valueOf(60000), person);
        Employment newEmployment = new Employment(LocalDate.now(), LocalDate.now().plusYears(1), "Company C", "Developer", BigDecimal.valueOf(70000), person);
        person.setEmployment(Set.of(existingEmployment));

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));

        assertThrows(EmploymentDateOverlapException.class, () -> {
            employmentService.createNewEmployment(personId, newEmployment);
        });

        verify(employmentRepository, never()).save(any(Employment.class));
    }

    @Test
    public void shouldUpdateEmployment() {
        Long personId = 1L;
        Long employmentId = 1L;
        Person person = new Person();
        Employment existingEmployment = new Employment(employmentId, LocalDate.now().minusYears(1), LocalDate.now(), "Company A", "Developer", BigDecimal.valueOf(50000), person);
        Employment updatedEmployment = new Employment(LocalDate.now().minusYears(1), LocalDate.now(), "Company A", "Senior Developer", BigDecimal.valueOf(60000), person);

        when(employmentRepository.findByIdAndPersonId(employmentId, personId)).thenReturn(Optional.of(existingEmployment));
        when(employmentRepository.save(existingEmployment)).thenReturn(existingEmployment);


        Employment result = employmentService.updateEmploymentHistory(personId, employmentId, updatedEmployment);


        assertNotNull(result);
        assertEquals("Senior Developer", result.getRole());
        assertEquals(BigDecimal.valueOf(60000), result.getSalary());
    }

    @Test
    public void shouldThrowExceptionWhenEmploymentNotFound() {
        Long personId = 1L;
        Long employmentId = 1L;
        Employment employment = new Employment();

        when(employmentRepository.findByIdAndPersonId(employmentId, personId)).thenReturn(Optional.empty());

        assertThrows(EmploymentNotFoundException.class, () -> {
            employmentService.updateEmploymentHistory(personId, employmentId, employment);
        });
    }

    @Test
    public void shouldDeleteEmployment() {
        Long personId = 1L;
        Long employmentId = 1L;
        Employment employment = new Employment();

        when(employmentRepository.findByIdAndPersonId(employmentId, personId)).thenReturn(Optional.of(employment));

        employmentService.removeEmployment(personId, employmentId);

        assertTrue(employmentRepository.findByIdAndPersonId(employmentId, personId).isPresent());
    }
}
