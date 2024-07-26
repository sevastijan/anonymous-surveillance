package pl.kurs.anonymoussurveillance.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.specifications.PersonSpecifications;

import java.util.Map;

@Service
@Transactional
public class PersonService {

//    @Autowired
//    private PersonRepository personRepository;
//
//    public Page<Person> findPersons(Map<String, String> criteria, Pageable pageable) {
//        Specification<Person> spec = Specification.where(null);
//
//        if (criteria.containsKey("firstName")) {
//            spec = spec.and(PersonSpecifications.hasFirstName(criteria.get("firstName")));
//        }
//        if (criteria.containsKey("lastName")) {
//            spec = spec.and(PersonSpecifications.hasLastName(criteria.get("lastName")));
//        }
//        if (criteria.containsKey("pesel")) {
//            spec = spec.and(PersonSpecifications.hasPesel(criteria.get("pesel")));
//        }
//        if (criteria.containsKey("minHeight") && criteria.containsKey("maxHeight")) {
//            spec = spec.and(PersonSpecifications.heightBetween(
//                    Integer.parseInt(criteria.get("minHeight")),
//                    Integer.parseInt(criteria.get("maxHeight"))
//            ));
//        }

        //TODO: Dodatkowe warunki dla innych pól

//        return personRepository.findAll(spec, pageable);
//    }
}
