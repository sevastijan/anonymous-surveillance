package pl.kurs.anonymoussurveillance.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kurs.anonymoussurveillance.models.AttributeType;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.models.RequiredAttribute;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;

@Service
public class PersonTypeService {
    private final PersonTypeRepository personTypeRepository;

    @Autowired
    public PersonTypeService(PersonTypeRepository personTypeRepository) {
        this.personTypeRepository = personTypeRepository;
    }
    public PersonType savePersonType(PersonType personType) {
        for(RequiredAttribute attribute : personType.getRequiredAttributes()) {
            if(attribute.getName() == null || attribute.getName().isEmpty()) {
                throw new IllegalArgumentException("Required attribute must have name");
            }

            if(attribute.getAttributeType() == null) {
                throw new IllegalArgumentException("Required attribute must have type");
            }

            boolean typeIsVaild = false;
            for (AttributeType type : AttributeType.values()) {
                if(attribute.getAttributeType() == type) {
                    typeIsVaild = true;
                    break;
                }
            }

            if(!typeIsVaild) {
                throw new IllegalArgumentException("Invaild attribute type: " + attribute.getAttributeType());
            }
        }

        return personTypeRepository.save(personType);
    }


}
