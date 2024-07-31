package pl.kurs.anonymoussurveillance.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.kurs.anonymoussurveillance.models.AttributeType;
import pl.kurs.anonymoussurveillance.models.RequiredAttribute;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                new RequiredAttribute("universityName", AttributeType.STRING),
                new RequiredAttribute("studyYear", AttributeType.INTEGER),
                new RequiredAttribute("fieldOfStudy", AttributeType.STRING),
                new RequiredAttribute("scholarshipAmount", AttributeType.DOUBLE)
        ));

        createDefaultPersonType("employee", Arrays.asList(
                new RequiredAttribute("employmentStartDate", AttributeType.DATE),
                new RequiredAttribute("role", AttributeType.STRING),
                new RequiredAttribute("salary", AttributeType.DOUBLE)
        ));

        createDefaultPersonType("retiree", Arrays.asList(
                new RequiredAttribute("pension", AttributeType.DOUBLE),
                new RequiredAttribute("yearsWorked", AttributeType.INTEGER)
        ));
    }

    private void createDefaultPersonType(String typeName, List<RequiredAttribute> attributes) {
        personTypeRepository.findByName(typeName).orElseGet(() -> {
            PersonType newType = new PersonType();
            newType.setName(typeName);
            newType.setRequiredAttributes(attributes.stream().map(attr -> {
                attr.setPersonType(newType);
                return attr;
            }).collect(Collectors.toList()));
            return personTypeRepository.save(newType);
        });
    }
}
