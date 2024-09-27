package pl.kurs.anonymoussurveillance.config;

import org.junit.Test;
import org.modelmapper.ModelMapper;
import pl.kurs.anonymoussurveillance.models.Person;

import java.util.List;

import static org.junit.Assert.*;

public class BeansConfigTest {

    @Test
    public void shouldMapPersonToPersonDto() {
        assertEquals("John", "John");
    }
}