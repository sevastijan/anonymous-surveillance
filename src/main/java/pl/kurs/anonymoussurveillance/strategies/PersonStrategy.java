package pl.kurs.anonymoussurveillance.strategies;

import pl.kurs.anonymoussurveillance.models.Person;

import java.util.Map;

public interface PersonStrategy {
    Person createPerson(Map<String, Object> persondata);

}
