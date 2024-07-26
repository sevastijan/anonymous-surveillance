package pl.kurs.anonymoussurveillance.strategies;

import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.services.PersonService;

import java.util.List;
import java.util.Map;

public interface PersonStrategy {
    Person savePerson(Person person);
    Person updatePerson(Person person);
    List<Person> searchPeople(Map<String, Object> searchParams);
}
