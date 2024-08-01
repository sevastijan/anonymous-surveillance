package pl.kurs.anonymoussurveillance.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import pl.kurs.anonymoussurveillance.dto.PersonAttributeCriteriaDto;
import pl.kurs.anonymoussurveillance.dto.PersonDto;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;
import pl.kurs.anonymoussurveillance.specifications.PersonSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/person")
public class PersonController {

    private PersonRepository personRepository;
    private ModelMapper modelMapper;

    public PersonController(PersonRepository personRepository, ModelMapper modelMapper) {
        this.personRepository = personRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public Page<PersonDto> searchPerson(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam Map<String, String> searchParams
    ) {
        Pageable pageable = PageRequest.of(page, size);

        PersonSearchCriteriaDto personSearchCriteriaDto = new PersonSearchCriteriaDto();
        List<PersonAttributeCriteriaDto> attributes = new ArrayList<>();
        Map<String, Number[]> numericRanges = new HashMap<>();

        searchParams.forEach((key, value) -> {
            if (!key.equals("page") && !key.equals("size")) {
                if (key.startsWith("min") || key.startsWith("max")) {
                    String attributeName = key.substring(3).toLowerCase();
                    Number numberValue = value.contains(".") ? Double.valueOf(value) : Integer.valueOf(value);

                    numericRanges.putIfAbsent(attributeName, new Number[2]);
                    if (key.startsWith("min")) {
                        numericRanges.get(attributeName)[0] = numberValue;
                    } else if (key.startsWith("max")) {
                        numericRanges.get(attributeName)[1] = numberValue;
                    }
                } else {
                    PersonAttributeCriteriaDto personAttributeCriteriaDto = new PersonAttributeCriteriaDto();
                    personAttributeCriteriaDto.setName(key);
                    personAttributeCriteriaDto.setValue(value);
                    attributes.add(personAttributeCriteriaDto);
                }
            }
        });

        personSearchCriteriaDto.setAttributes(attributes);
        personSearchCriteriaDto.setNumericRanges(numericRanges);

        Page<Person> personListPage = personRepository.findAll(PersonSpecification.createSpecification(personSearchCriteriaDto), pageable);

        List<PersonDto> personDtoList = personListPage.getContent().stream().map(p -> modelMapper.map(p, PersonDto.class))
                .collect(Collectors.toList());

        return new PageImpl<>(personDtoList, pageable, personListPage.getTotalElements());

    }
}
