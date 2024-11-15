package pl.kurs.anonymoussurveillance.config;

import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import pl.kurs.anonymoussurveillance.dto.PersonDto;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class BeansConfigTest {
    private BeansConfig beansConfig;
    private ModelMapper modelMapper;
    @Before
    public void setUp() {
        beansConfig = new BeansConfig();
        modelMapper = beansConfig.getModelMapper();
    }

    private PersonAttribute createPersonAttribute(String name, String value, Person person) {
        PersonAttribute attribute = new PersonAttribute();
        attribute.setName(name);
        attribute.setValue(value);
        attribute.setPerson(person);
        return attribute;
    }

    @Test
    public void shouldMapPersonToPersonDto() {
        Person person = new Person();
        Set<PersonAttribute> attributes = new HashSet<>();

        attributes.add(createPersonAttribute("firstName", "Jan", person));
        attributes.add(createPersonAttribute("lastName", "Kowalski", person));
        attributes.add(createPersonAttribute("pesel", "92122800098", person));
        attributes.add(createPersonAttribute("email", "janek@example.com", person));

        person.setAttributes(attributes);

        PersonDto personDto = modelMapper.map(person, PersonDto.class);

        assertEquals("Jan", personDto.getFirstName());
        assertEquals("Kowalski", personDto.getLastName());
        assertEquals("92122800098", personDto.getPesel());
        assertEquals("janek@example.com", personDto.getEmail());
    }

    @Test
    public void shouldReturnAttributeValue() {
        Person person = new Person();
        Set<PersonAttribute> attributes = new HashSet<>();
        attributes.add(createPersonAttribute("firstName", "Jan", person));
        person.setAttributes(attributes);

        String value = beansConfig.getAttributeValue(person, "firstName");

        assertEquals("Jan", value);
    }

    @Test
    public void shouldReturnNullWhenAttributesAreNull() {
        Person person = new Person();
        person.setAttributes(null);

        String value = beansConfig.getAttributeValue(person, "firstName");

        assertNull(value);
    }
}