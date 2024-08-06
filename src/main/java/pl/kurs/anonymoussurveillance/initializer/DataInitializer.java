package pl.kurs.anonymoussurveillance.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.anonymoussurveillance.models.AttributeType;
import pl.kurs.anonymoussurveillance.models.RequiredAttribute;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;

import java.util.ArrayList;
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

    @Transactional
    protected void createDefaultPersonTypes() {
        List<RequiredAttribute> commonAttributes = getCommonAttributes();

        createDefaultPersonType("student", combineAttributes(commonAttributes, Arrays.asList(
                new RequiredAttribute("universityName", AttributeType.STRING),
                new RequiredAttribute("studyYear", AttributeType.INTEGER),
                new RequiredAttribute("fieldOfStudy", AttributeType.STRING),
                new RequiredAttribute("scholarshipAmount", AttributeType.DOUBLE)
        )));

        createDefaultPersonType("employee", combineAttributes(commonAttributes, List.of()));

        createDefaultPersonType("retiree", combineAttributes(commonAttributes, Arrays.asList(
                new RequiredAttribute("pension", AttributeType.DOUBLE),
                new RequiredAttribute("yearsWorked", AttributeType.INTEGER)
        )));
    }

    private List<RequiredAttribute> getCommonAttributes() {
        return Arrays.asList(
                new RequiredAttribute("weight", AttributeType.DOUBLE),
                new RequiredAttribute("height", AttributeType.DOUBLE),
                new RequiredAttribute("pesel", AttributeType.STRING),
                new RequiredAttribute("firstName", AttributeType.STRING),
                new RequiredAttribute("lastName", AttributeType.STRING),
                new RequiredAttribute("email", AttributeType.STRING)
        );
    }

    private List<RequiredAttribute> combineAttributes(List<RequiredAttribute> commonAttributes, List<RequiredAttribute> specificAttributes) {
        List<RequiredAttribute> combinedAttributes = new ArrayList<>(specificAttributes);
        combinedAttributes.addAll(commonAttributes);
        return combinedAttributes;
    }

    @Transactional
    protected void createDefaultPersonType(String typeName, List<RequiredAttribute> attributes) {
        personTypeRepository.findByName(typeName).orElseGet(() -> {
            PersonType personType = new PersonType();
            personType.setName(typeName);
            personType.setRequiredAttributes(attributes.stream().map(attr -> {
                RequiredAttribute managedAttr = new RequiredAttribute(attr.getName(), attr.getAttributeType());
                managedAttr.setPersonType(personType);
                return managedAttr;
            }).collect(Collectors.toList()));
            return personTypeRepository.save(personType);
        });
    }
}
