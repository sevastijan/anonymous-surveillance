package pl.kurs.anonymoussurveillance.initializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PersonTypeRepository personTypeRepository;

    @Override
    public void run(String... args) throws Exception {
        createDefaultPersonTypes();
    }

    private void createDefaultPersonTypes() {
        createDefaultPersonType("student", Arrays.asList(
                new PersonAttribute("universityName", "String"),
                new PersonAttribute("studyYear", "Integer"),
                new PersonAttribute("fieldOfStudy", "String"),
                new PersonAttribute("scholarshipAmount", "BigDecimal")
        ));

        createDefaultPersonType("employee", Arrays.asList(
                new PersonAttribute("employmentStartDate", "Date"),
                new PersonAttribute("role", "String"),
                new PersonAttribute("salary", "BigDecimal")
        ));

        createDefaultPersonType("retiree", Arrays.asList(
                new PersonAttribute("pension", "BigDecimal"),
                new PersonAttribute("yearsWorked", "Integer")
        ));
    }

    private void createDefaultPersonType(String typeName, List<PersonAttribute> attributes) {
        personTypeRepository.findByName(typeName).orElseGet(() -> {
            PersonType newType = new PersonType();

            newType.setName(typeName);
            newType.setAttributes(attributes);

            return personTypeRepository.save(newType);
        });
    }
}
