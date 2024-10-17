package pl.kurs.anonymoussurveillance.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import pl.kurs.anonymoussurveillance.commands.CreatePersonTypeCommand;
import pl.kurs.anonymoussurveillance.dto.ErrorDto;
import pl.kurs.anonymoussurveillance.dto.PersonTypeDto;
import pl.kurs.anonymoussurveillance.models.AttributeType;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.models.RequiredAttribute;
import pl.kurs.anonymoussurveillance.services.PersonTypeService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class PersonTypeControllerTest {

    @Mock
    private PersonTypeService personTypeService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PersonTypeController personTypeController;

    private CreatePersonTypeCommand createPersonTypeCommand;
    private PersonType personType;
    private PersonTypeDto personTypeDto;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        createPersonTypeCommand = new CreatePersonTypeCommand();
        createPersonTypeCommand.setName("employee");

        RequiredAttribute attribute1 = new RequiredAttribute();
        attribute1.setName("attribute1");
        attribute1.setAttributeType(AttributeType.STRING);

        RequiredAttribute attribute2 = new RequiredAttribute();
        attribute2.setName("attribute2");
        attribute2.setAttributeType(AttributeType.INTEGER);

        createPersonTypeCommand.setAttributes(Arrays.asList(attribute1, attribute2));

        personType = new PersonType();
        personType.setName("employee");
        personType.setRequiredAttributes(Arrays.asList(
                new RequiredAttribute("attribute1", AttributeType.STRING),
                new RequiredAttribute("attribute2", AttributeType.INTEGER)
        ));


        personTypeDto = new PersonTypeDto();
        personTypeDto.setId(1L);
        personTypeDto.setName("employee");
    }

    @Test
    public void shouldCreatePersonTypeSuccessfully() {
        when(modelMapper.map(createPersonTypeCommand, PersonType.class)).thenReturn(personType);
        when(modelMapper.map(personType, PersonTypeDto.class)).thenReturn(personTypeDto);

        ResponseEntity<PersonTypeDto> response = personTypeController.createPersonType(createPersonTypeCommand);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("employee", response.getBody().getName());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    public void shouldReturnBadRequestWhenRequiredAttributesAreEmpty() {
        //TODO: verify
        createPersonTypeCommand.setAttributes(Collections.emptyList());

        assertThrows(ResponseStatusException.class, () -> {
            personTypeController.createPersonType(createPersonTypeCommand);
        });
    }

    @Test
    public void shouldReturnBadRequestWhenRequiredAttributesAreNull() {
        //TODO: verify
        createPersonTypeCommand.setAttributes(null);

        assertThrows(ResponseStatusException.class, () -> {
            personTypeController.createPersonType(createPersonTypeCommand);
        });
    }

    @Test
    public void shouldHandleServiceException() {
        when(modelMapper.map(createPersonTypeCommand, PersonType.class)).thenReturn(personType);
        when(modelMapper.map(personType, PersonTypeDto.class)).thenReturn(personTypeDto);

        doThrow(new OptimisticLockingFailureException("Optimistic lock exception"))
                .when(personTypeService).savePersonType(personType);

        OptimisticLockingFailureException exception = assertThrows(OptimisticLockingFailureException.class, () -> {
            personTypeController.createPersonType(createPersonTypeCommand);
        });

        assertEquals("Optimistic lock exception", exception.getMessage());
    }

    @Test
    public void shouldHandleResponseStatusException() {
        ResponseStatusException responseStatusException = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");

        ResponseEntity<ErrorDto> response = personTypeController.handleResponseStatusException(responseStatusException);

        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
    }
}