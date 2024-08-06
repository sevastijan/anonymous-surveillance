package pl.kurs.anonymoussurveillance.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.anonymoussurveillance.models.EmploymentHistory;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.repositories.EmploymentHistoryRepository;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmploymentHistoryService {
    private final PersonRepository personRepository;
    private final EmploymentHistoryRepository employmentHistoryRepository;

    @Transactional
    public Person createNewEmployment(Long personId, EmploymentHistory employmentHistory) {
        //TODO: add custom exception
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Person with id:" + personId + " not found."));

        if(hasOverlapEmploymentDates(person.getEmploymentHistory(), employmentHistory)) {
            //TODO: add custom exception
            throw new IllegalArgumentException("Can't add new employment because dates overlap with an existing position");
        }

        employmentHistory.setPerson(person);
        employmentHistoryRepository.save(employmentHistory);

        return person;
    }

    /**
     * hasOverlapEmploymentDates - checks if you can add new employment into person employment history to avoid over-employment
     * @param employmentHistoryList current person employment history
     * @param newEmployment new job position to be added into employment history
     * @return true if dates are overlapping
     */
    private boolean hasOverlapEmploymentDates(List<EmploymentHistory> employmentHistoryList, EmploymentHistory newEmployment) {
        for(EmploymentHistory employmentHistory : employmentHistoryList) {
            if(newEmployment.getStartDate().isBefore(employmentHistory.getEndDate()) && newEmployment.getEndDate().isAfter(employmentHistoryList.getFirst().getStartDate())) {
                return true;
            }
        }

        return false;
    }
}
