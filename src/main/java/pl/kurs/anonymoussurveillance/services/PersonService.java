package pl.kurs.anonymoussurveillance.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.kurs.anonymoussurveillance.commands.CreatePersonCommand;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;
import pl.kurs.anonymoussurveillance.specifications.PersonSpecifications;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonTypeRepository personTypeRepository;

//    public Person createPerson(CreatePersonCommand command) {
//        Optional<PersonType> personTypeOpt = personTypeRepository.findByName(command.getPersonType());
//        if (personTypeOpt.isEmpty()) {
//            throw new IllegalArgumentException("Unknown type: " + command.getPersonType());
//        }
//
//        PersonType personType = personTypeOpt.get();
//        personType.getAttributes().size();
//
//        Person person = new Person.Builder()
//                .setFirstName(command.getFirstName())
//                .setLastName(command.getLastName())
//                .setPesel(command.getPesel())
//                .setHeight(command.getHeight())
//                .setWeight(command.getWeight())
//                .setEmail(command.getEmail())
//                .setPersonType(personType)
//                .setAttributes(command.getAttributes())
//                .build();
//
//        return personRepository.save(person);
//    }
}