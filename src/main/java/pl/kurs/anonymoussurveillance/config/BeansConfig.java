package pl.kurs.anonymoussurveillance.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kurs.anonymoussurveillance.dto.PersonDto;
import pl.kurs.anonymoussurveillance.dto.PersonTypeDto;
import pl.kurs.anonymoussurveillance.dto.PersonAttributeDto;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

@Configuration
public class BeansConfig {
    @Bean
    public ModelMapper getModelMapper() {
        ModelMapper mapper = new ModelMapper();

        TypeMap<Person, PersonDto> personTypeMap = mapper.createTypeMap(Person.class, PersonDto.class);
        personTypeMap.addMappings(new PropertyMap<>() {
            @Override
            protected void configure() {
                using(ctx -> getAttributeValue((Person) ctx.getSource(), "firstName")).map(source, destination.getFirstName());
                using(ctx -> getAttributeValue((Person) ctx.getSource(), "lastName")).map(source, destination.getLastName());
                using(ctx -> getAttributeValue((Person) ctx.getSource(), "pesel")).map(source, destination.getPesel());
                using(ctx -> getAttributeValue((Person) ctx.getSource(), "email")).map(source, destination.getEmail());
                map(source.getPersonType(), destination.getPersonType());
                map(source.getAttributes(), destination.getAttributes());
            }
        });

        TypeMap<PersonType, PersonTypeDto> personTypeTypeMap = mapper.createTypeMap(PersonType.class, PersonTypeDto.class);
        personTypeTypeMap.addMappings(new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setName(source.getName());
            }
        });


        TypeMap<PersonAttribute, PersonAttributeDto> personAttributeTypeMap = mapper.createTypeMap(PersonAttribute.class, PersonAttributeDto.class);
        personAttributeTypeMap.addMappings(new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setName(source.getName());
                map().setValue(source.getValue());
            }
        });

        return mapper;
    }

    private String getAttributeValue(Person person, String attributeName) {
        if (person.getAttributes() != null) {
            return person.getAttributes().stream()
                    .filter(attr -> attr.getName().equals(attributeName))
                    .map(PersonAttribute::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
