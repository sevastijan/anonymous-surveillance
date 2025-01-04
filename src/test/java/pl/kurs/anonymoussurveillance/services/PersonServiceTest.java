package pl.kurs.anonymoussurveillance.services;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.OptimisticLockingFailureException;
import pl.kurs.anonymoussurveillance.commands.UpdatePersonAttributeCommand;
import pl.kurs.anonymoussurveillance.commands.UpdatePersonCommand;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private PersonService personService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldUpdateAttributesWhenPersonExistsAndVersionMatches() {
        Long personId = 1L;
        Long version = 1L;
        Person person = new Person();
        person.setId(personId);
        person.setVersion(version);

        Set<PersonAttribute> attributes = new HashSet<>() {};
        PersonAttribute existingAttribute = new PersonAttribute();
        existingAttribute.setName("height");
        existingAttribute.setAttribute_value("180");
        attributes.add(existingAttribute);
        person.setAttributes(attributes);

        UpdatePersonCommand updateCommand = new UpdatePersonCommand();
        updateCommand.setId(personId);
        updateCommand.setVersion(version);
        UpdatePersonAttributeCommand updateHeightCommand = new UpdatePersonAttributeCommand("height", "185");
        UpdatePersonAttributeCommand newAttributeCommand = new UpdatePersonAttributeCommand("weight", "80");
        updateCommand.setAttributes(List.of(updateHeightCommand, newAttributeCommand));

        when(personRepository.findByIdWithAttributes(personId)).thenReturn(Optional.of(person));

        Person result = personService.updatePerson(updateCommand);

        assertNotNull(result);
        assertEquals("185", result.getAttributes().stream()
                .filter(attr -> attr.getName().equals("height"))
                .findFirst().get().getAttribute_value());

        assertEquals("80", result.getAttributes().stream()
                .filter(attr -> attr.getName().equals("weight"))
                .findFirst().get().getAttribute_value());
    }

    @Test
    public void shouldThrowExceptionWhenPersonNotFound() {
        Long personId = 1L;
        UpdatePersonCommand updateCommand = new UpdatePersonCommand();
        updateCommand.setId(personId);
        when(personRepository.findByIdWithAttributes(personId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            personService.updatePerson(updateCommand);
        });
    }

    @Test
    public void shouldThrowExceptionWhenVersionMismatch() {
        Long personId = 1L;
        Long version = 1L;
        Person person = new Person();
        person.setId(personId);
        person.setVersion(version);

        UpdatePersonCommand updateCommand = new UpdatePersonCommand();
        updateCommand.setId(personId);
        updateCommand.setVersion(2L);

        when(personRepository.findByIdWithAttributes(personId)).thenReturn(Optional.of(person));

        assertThrows(OptimisticLockingFailureException.class, () -> {
            personService.updatePerson(updateCommand);
        });
    }

    @Test
    public void shouldAddNewAttributeWhenAttributeDoesNotExist() {
        Long personId = 1L;
        Long version = 1L;
        Person person = new Person();
        person.setId(personId);
        person.setVersion(version);

        person.setAttributes(new HashSet<>());

        UpdatePersonCommand updateCommand = new UpdatePersonCommand();
        updateCommand.setId(personId);
        updateCommand.setVersion(version);

        UpdatePersonAttributeCommand newAttributeCommand = new UpdatePersonAttributeCommand("weight", "80");
        updateCommand.setAttributes(List.of(newAttributeCommand));

        when(personRepository.findByIdWithAttributes(personId)).thenReturn(Optional.of(person));

        Person result = personService.updatePerson(updateCommand);

        assertNotNull(result);
        assertEquals(1, result.getAttributes().size());
        assertEquals("80", result.getAttributes().stream()
                .filter(attr -> attr.getName().equals("weight"))
                .findFirst().get().getAttribute_value());
    }

    @Test
    public void shouldProcessSearchCriteriaWithPesel() {
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("pesel", "12345678901");

        PersonSearchCriteriaDto result = personService.processSearchCriteria(searchParams);

        assertEquals("12345678901", result.getPesel());
        assertFalse(result.getAttributes().isEmpty());
    }

    @Test
    public void shouldIgnorePageAndSizeParameters() {
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("page", "1");
        searchParams.put("size", "10");
        searchParams.put("pesel", "12345678901");

        PersonSearchCriteriaDto result = personService.processSearchCriteria(searchParams);

        assertEquals("12345678901", result.getPesel());
        assertFalse(result.getAttributes().isEmpty());
    }

    @Test
    public void shouldProcessEmptySearchParams() {
        Map<String, String> searchParams = new HashMap<>();

        PersonSearchCriteriaDto result = personService.processSearchCriteria(searchParams);

        assertNull(result.getPesel());
        assertTrue(result.getAttributes().isEmpty());
    }

    @Test
    public void shouldProcessSearchCriteriaWithMultipleParams() {
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("pesel", "12345678901");
        searchParams.put("page", "1");
        searchParams.put("size", "10");
        searchParams.put("age", "25");

        PersonSearchCriteriaDto result = personService.processSearchCriteria(searchParams);

        assertEquals("12345678901", result.getPesel());
    }

    @Test
    public void shouldHandleNullSearchParams() {
        Map<String, String> searchParams = null;

        assertThrows(NullPointerException.class, () -> {
            personService.processSearchCriteria(searchParams);
        });
    }
}
