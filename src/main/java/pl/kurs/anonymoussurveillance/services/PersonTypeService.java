package pl.kurs.anonymoussurveillance.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;

@Service
public class PersonTypeService {
    private final PersonTypeRepository personTypeRepository;

    @Autowired
    public PersonTypeService(PersonTypeRepository personTypeRepository) {
        this.personTypeRepository = personTypeRepository;
    }
    public PersonType savePersonType(PersonType personType) {
        return personTypeRepository.save(personType);
    }
}
