package pl.kurs.anonymoussurveillance.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.kurs.anonymoussurveillance.models.*;
import pl.kurs.anonymoussurveillance.repositories.PersonTypeRepository;
import pl.kurs.anonymoussurveillance.repositories.RoleRepository;
import pl.kurs.anonymoussurveillance.repositories.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {
    /**
     * this file is kind of initializer to developing locally, creating default values
     */

    @Autowired
    private PersonTypeRepository personTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDefaultPersonTypes();
        createDefaultRoles();
        createDefaultUsers();
    }

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


    protected void createDefaultRoles() {
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_USER");
        createRoleIfNotFound("ROLE_IMPORTER");
        createRoleIfNotFound("ROLE_EMPLOYEE");
    }


    protected void createRoleIfNotFound(String roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    protected void createDefaultUsers() {
        createDefaultUser("admin", "admin", List.of("ROLE_ADMIN"));
        createDefaultUser("user", "user", List.of("ROLE_USER"));
        createDefaultUser("importer", "importer", List.of("ROLE_IMPORTER"));
        createDefaultUser("employee", "employee", List.of("ROLE_EMPLOYEE"));
    }

    protected void createDefaultUser(String username, String password, List<String> roleList) {
        if(userRepository.findByUsername(username).isEmpty()) {
            User defaultUser = new User();
            defaultUser.setUsername(username);
            defaultUser.setPassword(passwordEncoder.encode(password));

            defaultUser.setRoles(roleList.stream()
                    .map(roleRepository::findByName)
                    .map(Optional::get)
                    .collect(Collectors.toList()));
            userRepository.save(defaultUser);
        }
    }
}
