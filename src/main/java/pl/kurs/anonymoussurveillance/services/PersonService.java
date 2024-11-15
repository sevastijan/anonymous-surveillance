package pl.kurs.anonymoussurveillance.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.anonymoussurveillance.commands.UpdatePersonAttributeCommand;
import pl.kurs.anonymoussurveillance.commands.UpdatePersonCommand;
import pl.kurs.anonymoussurveillance.dto.PersonAttributeCriteriaDto;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;
import pl.kurs.anonymoussurveillance.strategies.SearchStrategy;
import pl.kurs.anonymoussurveillance.factories.SearchStrategyFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor

public class PersonService {
    private final PersonRepository personRepository;
    private final EntityManager entityManager;

    public PersonSearchCriteriaDto processSearchCriteria(Map<String, String> searchParams) {

        PersonSearchCriteriaDto personSearchCriteriaDto = new PersonSearchCriteriaDto();
        List<PersonAttributeCriteriaDto> attributes = new ArrayList<>();
        Map<String, Number[]> numberRange = new HashMap<>();
        Map<String, LocalDate[]> dateRange = new HashMap<>();

        personSearchCriteriaDto.setAttributes(attributes);
        personSearchCriteriaDto.setNumberRange(numberRange);
        personSearchCriteriaDto.setDateRange(dateRange);

        searchParams.forEach((key, value) -> {
            if (!key.equals("page") && !key.equals("size")) {
                SearchStrategy searchStrategy = SearchStrategyFactory.getStrategy(key);
                searchStrategy.process(key, value, personSearchCriteriaDto);
            }
            if(key.equals("pesel")) {
                personSearchCriteriaDto.setPesel(value);
            }
        });

        return personSearchCriteriaDto;
    }

    @Transactional
    public Person updatePerson(UpdatePersonCommand updatePersonCommand) {
        Person person = personRepository.findByIdWithAttributes(updatePersonCommand.getId()).orElseThrow(
                () -> new IllegalArgumentException("Person not found"));

        if (!person.getVersion().equals(updatePersonCommand.getVersion())) {
            throw new OptimisticLockingFailureException("Version mismatch");
        }

        entityManager.lock(person, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        Map<String, PersonAttribute> personAttributeMap = new HashMap<>();

        for(PersonAttribute personAttribute: person.getAttributes()) {
            personAttributeMap.put(personAttribute.getName(), personAttribute);
        }

        for(UpdatePersonAttributeCommand updatePersonAttributeCommand : updatePersonCommand.getAttributes()) {
            if(personAttributeMap.containsKey(updatePersonAttributeCommand.getName())) {
                PersonAttribute existingPersonAttribute = personAttributeMap.get(updatePersonAttributeCommand.getName());
                existingPersonAttribute.setValue(updatePersonAttributeCommand.getValue());
            } else {
                PersonAttribute newPersonAttributes = new PersonAttribute();
                newPersonAttributes.setName(updatePersonAttributeCommand.getName());
                newPersonAttributes.setValue(updatePersonAttributeCommand.getValue());
                newPersonAttributes.setPerson(person);
                person.getAttributes().add(newPersonAttributes);
            }
        }

        return person;
    }

}