package pl.kurs.anonymoussurveillance.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.kurs.anonymoussurveillance.commands.CreateEmploymentCommand;
import pl.kurs.anonymoussurveillance.commands.UpdateEmploymentCommand;
import pl.kurs.anonymoussurveillance.commands.UpdatePersonCommand;
import pl.kurs.anonymoussurveillance.dto.EmploymentDto;
import pl.kurs.anonymoussurveillance.dto.ErrorDto;
import pl.kurs.anonymoussurveillance.dto.PersonDto;
import pl.kurs.anonymoussurveillance.exceptions.EmploymentDateOverlapException;
import pl.kurs.anonymoussurveillance.exceptions.EmploymentNotFoundException;
import pl.kurs.anonymoussurveillance.exceptions.PersonNotFoundException;
import pl.kurs.anonymoussurveillance.models.Employment;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;
import pl.kurs.anonymoussurveillance.services.EmploymentService;
import pl.kurs.anonymoussurveillance.services.PersonService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PersonControllerTest {

    private PersonController personController;

    private PersonRepository personRepository;
    @Mock
    private ModelMapper modelMapper;

    private PersonService personService;
    private EmploymentService employmentService;

    @BeforeEach
    public void setUp() {
        personRepository = mock(PersonRepository.class);
        modelMapper = mock(ModelMapper.class);
        personService = mock(PersonService.class);
        employmentService = mock(EmploymentService.class);

        personController = new PersonController(personRepository, modelMapper, personService, employmentService);
    }

    @Test
    public void shouldReturnPersonPageWhenSearchPerson() {
        int page = 0;
        int size = 100;
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("name", "Jasiu");

        Pageable pageable = PageRequest.of(page, size);

        Person person = new Person();
        person.setId(1L);

        PersonDto personDto = new PersonDto();
        personDto.setId(1L);

        Page<Person> personPage = new PageImpl<>(Collections.singletonList(person), pageable, 1);

        when(personRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(personPage);
        when(modelMapper.map(person, PersonDto.class)).thenReturn(personDto);

        ResponseEntity<Page<PersonDto>> response = personController.searchPerson(page, size, searchParams);
        Page<PersonDto> responseBody = response.getBody();
        PersonDto resultDto = responseBody.getContent().get(0);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(responseBody);
        assertEquals(1, responseBody.getTotalElements());
        assertEquals(1L, resultDto.getId());
    }

    @Test
    public void shouldUpdatePerson() {
        UpdatePersonCommand updatePersonCommand = new UpdatePersonCommand();
        updatePersonCommand.setId(1L);

        Person updatedPerson = new Person();
        updatedPerson.setId(1L);

        PersonDto personDto = new PersonDto();
        personDto.setId(1L);

        when(personService.updatePerson(any(UpdatePersonCommand.class))).thenReturn(updatedPerson);
        when(modelMapper.map(updatedPerson, PersonDto.class)).thenReturn(personDto);

        ResponseEntity<PersonDto> response = personController.updatePerson(updatePersonCommand);
        PersonDto responseBody = response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(responseBody);
        assertEquals(1L, responseBody.getId());
    }

    @Test
    public void shouldThrowExceptionWhenPersonNotFound() {
        UpdatePersonCommand updatePersonCommand = new UpdatePersonCommand();
        updatePersonCommand.setId(1L);

        when(personService.updatePerson(any(UpdatePersonCommand.class)))
                .thenThrow(new PersonNotFoundException("Person not found"));

        assertThrows(PersonNotFoundException.class, () -> personController.updatePerson(updatePersonCommand));
    }

    @Test
    public void shouldCreateNewEmploymentSuccessfully() {
        Long personId = 1L;
        CreateEmploymentCommand command = new CreateEmploymentCommand();

        Employment employment = new Employment();
        Person person = new Person();
        person.setId(personId);
        person.setEmployment(List.of(employment));

        when(modelMapper.map(command, Employment.class)).thenReturn(employment);
        when(employmentService.createNewEmployment(eq(personId), eq(employment))).thenReturn(person);
        when(modelMapper.map(employment, EmploymentDto.class)).thenReturn(new EmploymentDto());

        ResponseEntity<List<EmploymentDto>> response = personController.createNewEmployment(personId, command);
        List<EmploymentDto> responseBody = response.getBody();

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(responseBody);
        assertFalse(responseBody.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenEmploymentDateOverlaps() {
        Long personId = 1L;
        CreateEmploymentCommand command = new CreateEmploymentCommand();
        Employment employment = new Employment();

        when(modelMapper.map(command, Employment.class)).thenReturn(employment);
        when(employmentService.createNewEmployment(eq(personId), eq(employment)))
                .thenThrow(new EmploymentDateOverlapException("Employment date overlaps"));

        EmploymentDateOverlapException exception = assertThrows(EmploymentDateOverlapException.class, () -> {
            personController.createNewEmployment(personId, command);
        });

        assertEquals("Employment date overlaps", exception.getMessage());
    }


    @Test
    public void shouldThrowExceptionWhenPersonNotFoundInCreateEmployment() {
        Long personId = 1L;
        CreateEmploymentCommand createEmploymentCommand = new CreateEmploymentCommand();

        Employment employment = new Employment();

        when(modelMapper.map(createEmploymentCommand, Employment.class)).thenReturn(employment);

        when(employmentService.createNewEmployment(eq(personId), eq(employment)))
                .thenThrow(new PersonNotFoundException("Person not found"));

        assertThrows(PersonNotFoundException.class, () -> {
            personController.createNewEmployment(personId, createEmploymentCommand);
        });
    }

    @Test
    public void shouldUpdateEmployment() {
        Long personId = 1L;
        Long employmentId = 1L;
        UpdateEmploymentCommand command = new UpdateEmploymentCommand();

        Employment employment = new Employment();
        Employment updatedEmployment = new Employment();
        EmploymentDto employmentDto = new EmploymentDto();

        when(modelMapper.map(command, Employment.class)).thenReturn(employment);
        when(employmentService.updateEmploymentHistory(eq(personId), eq(employmentId), eq(employment)))
                .thenReturn(updatedEmployment);
        when(modelMapper.map(updatedEmployment, EmploymentDto.class)).thenReturn(employmentDto);

        ResponseEntity<EmploymentDto> response = personController.updateEmployment(personId, employmentId, command);
        EmploymentDto responseBody = response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(responseBody);
    }

    @Test
    public void shouldThrowExceptionWhenEmploymentNotFound() {
        Long personId = 1L;
        Long employmentId = 1L;

        UpdateEmploymentCommand command = new UpdateEmploymentCommand();
        Employment employment = new Employment();

        when(modelMapper.map(command, Employment.class)).thenReturn(employment);
        when(employmentService.updateEmploymentHistory(eq(personId), eq(employmentId), eq(employment)))
                .thenThrow(new EmploymentNotFoundException(employmentId, personId));

        EmploymentNotFoundException exception = assertThrows(EmploymentNotFoundException.class, () -> {
            personController.updateEmployment(personId, employmentId, command);
        });

        assertEquals("Employment with id: 1 and person id: 1 not found.", exception.getMessage());
    }

    @Test
    public void shouldDeleteEmploymentSuccessfully() {
        Long personId = 1L;
        Long employmentId = 1L;

        doNothing().when(employmentService).removeEmployment(personId, employmentId);

        ResponseEntity<Void> response = personController.deleteEmployment(personId, employmentId);

        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody());
    }

//    @Test
//    public void shouldThrowExceptionWhenEmploymentNotFoundOnDelete() {
//        Long personId = 1L;
//        Long employmentId = 1L;
//
//        doThrow(new EmploymentNotFoundException(personId, employmentId))
//                .when(employmentService).removeEmployment(personId, employmentId);
//
//        assertThrows(EmploymentNotFoundException.class, () -> personController.deleteEmployment(personId, employmentId));
//    }

    @Test
    public void shouldHandlePersonNotFoundException() {
        PersonNotFoundException exception = new PersonNotFoundException("Person not found");

        ResponseEntity<ErrorDto> response = personController.handlePersonNotFoundException(exception);
        ErrorDto errorDto = response.getBody();

        assertNotNull(errorDto);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.getStatus());
        assertEquals("Person not found", errorDto.getMessage());
    }

    @Test
    public void shouldHandleOptimisticLockingFailureException() {
        OptimisticLockingFailureException exception = new OptimisticLockingFailureException("Optimistic locking failure");
        ResponseEntity<ErrorDto> response = personController.handleOptimisticLockingFailureException(exception);
        ErrorDto errorDto = response.getBody();

        assertNotNull(errorDto);
        assertEquals(HttpStatus.LOCKED.value(), errorDto.getStatus());
        assertEquals("Optimistic locking failure", errorDto.getMessage());
    }

    @Test
    public void shouldReturnPersonPageWhenSearchPersonWithNumberRange() {
        int page = 0;
        int size = 100;
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("minAge", "18");
        searchParams.put("maxAge", "30");

        Pageable pageable = PageRequest.of(page, size);

        Person person = new Person();
        person.setId(1L);

        PersonDto personDto = new PersonDto();
        personDto.setId(1L);

        Page<Person> personPage = new PageImpl<>(Collections.singletonList(person), pageable, 1);

        when(personRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(personPage);
        when(modelMapper.map(person, PersonDto.class)).thenReturn(personDto);

        ResponseEntity<Page<PersonDto>> response = personController.searchPerson(page, size, searchParams);
        Page<PersonDto> responseBody = response.getBody();
        PersonDto resultDto = responseBody.getContent().get(0);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(responseBody);
        assertEquals(1, responseBody.getTotalElements());
        assertEquals(1L, resultDto.getId());
    }

    @Test
    public void shouldReturnPersonPageWhenSearchPersonWithDateRange() {
        int page = 0;
        int size = 100;
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("minBirthDate", "1990-01-01");
        searchParams.put("maxBirthDate", "2000-12-31");

        Pageable pageable = PageRequest.of(page, size);

        Person person = new Person();
        person.setId(1L);

        PersonDto personDto = new PersonDto();
        personDto.setId(1L);

        Page<Person> personPage = new PageImpl<>(Collections.singletonList(person), pageable, 1);

        when(personRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(personPage);
        when(modelMapper.map(person, PersonDto.class)).thenReturn(personDto);

        ResponseEntity<Page<PersonDto>> response = personController.searchPerson(page, size, searchParams);
        Page<PersonDto> responseBody = response.getBody();
        PersonDto resultDto = responseBody.getContent().get(0);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(responseBody);
        assertEquals(1, responseBody.getTotalElements());
        assertEquals(1L, resultDto.getId());
    }

    @Test
    public void shouldReturnPersonPageWhenSearchPersonWithMixedParams() {
        int page = 0;
        int size = 100;
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("minAge", "25");
        searchParams.put("maxAge", "40");
        searchParams.put("minStartDate", "2020-01-01");
        searchParams.put("maxStartDate", "2021-01-01");
        searchParams.put("name", "John");
        searchParams.put("surname", "Doe");

        Pageable pageable = PageRequest.of(page, size);

        Person person = new Person();
        person.setId(1L);

        PersonDto personDto = new PersonDto();
        personDto.setId(1L);

        Page<Person> personPage = new PageImpl<>(Collections.singletonList(person), pageable, 1);

        when(personRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(personPage);
        when(modelMapper.map(person, PersonDto.class)).thenReturn(personDto);

        ResponseEntity<Page<PersonDto>> response = personController.searchPerson(page, size, searchParams);
        Page<PersonDto> responseBody = response.getBody();
        PersonDto resultDto = responseBody.getContent().get(0);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(responseBody);
        assertEquals(1, responseBody.getTotalElements());
        assertEquals(1L, resultDto.getId());
    }
}
