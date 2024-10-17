package pl.kurs.anonymoussurveillance.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kurs.anonymoussurveillance.models.AttributeType;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.models.RequiredAttribute;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PersonTypeServiceTest {

    @Mock
    private PersonTypeRepository personTypeRepository;

    @InjectMocks
    private PersonTypeService personTypeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldSavePersonType() {
        PersonType personType = new PersonType();
        RequiredAttribute attribute = new RequiredAttribute("Height", AttributeType.INTEGER);
        personType.setRequiredAttributes(Collections.singletonList(attribute));

        when(personTypeRepository.save(any(PersonType.class))).thenReturn(personType);

        PersonType result = personTypeService.savePersonType(personType);

        assertNotNull(result);
    }

    @Test
    public void shouldThrowExceptionWhenAttributeNameIsNull() {
        PersonType personType = new PersonType();
        RequiredAttribute attribute = new RequiredAttribute(null, AttributeType.STRING);
        personType.setRequiredAttributes(Collections.singletonList(attribute));

        assertThrows(IllegalArgumentException.class, () -> {
            personTypeService.savePersonType(personType);
        }, "Required attribute must have name");
    }

    @Test
    public void shouldThrowExceptionWhenAttributeTypeIsNull() {
        PersonType personType = new PersonType();
        RequiredAttribute attribute = new RequiredAttribute("Height", null);
        personType.setRequiredAttributes(Collections.singletonList(attribute));

        assertThrows(IllegalArgumentException.class, () -> {
            personTypeService.savePersonType(personType);
        }, "Required attribute must have type");
    }
}
