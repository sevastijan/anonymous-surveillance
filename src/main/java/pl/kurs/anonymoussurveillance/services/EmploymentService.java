package pl.kurs.anonymoussurveillance.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.anonymoussurveillance.exceptions.EmploymentDateOverlapException;
import pl.kurs.anonymoussurveillance.exceptions.PersonNotFoundException;
import pl.kurs.anonymoussurveillance.models.Employment;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.repositories.EmploymentRepository;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmploymentService {
    private final PersonRepository personRepository;
    private final EmploymentRepository employmentRepository;

    @Transactional
    public Person createNewEmployment(Long personId, Employment employment) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new PersonNotFoundException("Person with id:" + personId + " not found."));

        if(hasOverlapEmploymentDates(person.getEmployment(), employment)) {
            throw new EmploymentDateOverlapException("Can't add new employment because dates overlap with an existing position");
        }

        employment.setPerson(person);
        employmentRepository.save(employment);

        return person;
    }

    @Transactional
    public Employment updateEmploymentHistory(Long personId, Long employmentId, Employment employment) {
        Employment selectedEmployment = employmentRepository.findByIdAndPersonId(employmentId, personId)
                .orElseThrow(() -> new IllegalArgumentException("Employment history with id:" + employmentId + " not found."));

        if(hasOverlapEmploymentDates(selectedEmployment.getPerson().getEmployment(), employment)) {
            //TODO: add custom exception
            throw new IllegalArgumentException("Can't add new employment because dates overlap with an existing position");
        }

        selectedEmployment.setCompanyName(employment.getCompanyName());
        selectedEmployment.setRole(employment.getRole());
        selectedEmployment.setStartDate(employment.getStartDate());
        selectedEmployment.setEndDate(employment.getEndDate());
        selectedEmployment.setSalary(employment.getSalary());

        return employmentRepository.save(selectedEmployment);
    }

    @Transactional
    public void removeEmployment(Long personId, Long employmentId) {
        //TODO: add custom exception
        Employment selectedEmployment = employmentRepository.findByIdAndPersonId(employmentId, personId)
                .orElseThrow(() -> new IllegalArgumentException("Employment history with id:" + employmentId + " not found."));

        employmentRepository.delete(selectedEmployment);
    }

    private boolean hasOverlapEmploymentDates(List<Employment> employmentList, Employment newEmployment) {
        for(Employment employment : employmentList) {
            if(newEmployment.getStartDate().isBefore(employment.getEndDate()) && newEmployment.getEndDate().isAfter(employmentList.get(0).getStartDate())) {
                return true;
            }
        }

        return false;
    }
}
