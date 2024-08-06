package pl.kurs.anonymoussurveillance.controllers;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kurs.anonymoussurveillance.commands.UpdatePersonCommand;
import pl.kurs.anonymoussurveillance.dto.PersonAttributeCriteriaDto;
import pl.kurs.anonymoussurveillance.dto.PersonDto;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;
import pl.kurs.anonymoussurveillance.models.EmploymentHistory;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;
import pl.kurs.anonymoussurveillance.services.EmploymentHistoryService;
import pl.kurs.anonymoussurveillance.services.PersonService;
import pl.kurs.anonymoussurveillance.specifications.PersonSpecification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/person")
public class PersonController {

    private final PersonRepository personRepository;
    private final ModelMapper modelMapper;
    private final PersonService personService;
    private final EmploymentHistoryService employmentHistoryService;

    @GetMapping
    public ResponseEntity<Page<PersonDto>> searchPerson(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam Map<String, String> searchParams
    ) {
        Pageable pageable = PageRequest.of(page, size);

        PersonSearchCriteriaDto personSearchCriteriaDto = new PersonSearchCriteriaDto();
        List<PersonAttributeCriteriaDto> attributes = new ArrayList<>();
        Map<String, Number[]> numberRange = new HashMap<>();
        Map<String, LocalDate[]> dateRange = new HashMap<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");



        searchParams.forEach((key, value) -> {
            if (!key.equals("page") && !key.equals("size")) {
                if (key.startsWith("min") || key.startsWith("max")) {
                    String attributeName = key.substring(3);
                    if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        LocalDate dateValue = LocalDate.parse(value, dateTimeFormatter);
                        dateRange.putIfAbsent(attributeName, new LocalDate[2]);
                        if (key.startsWith("min")) {
                            dateRange.get(attributeName)[0] = dateValue;
                        } else if (key.startsWith("max")) {
                            dateRange.get(attributeName)[1] = dateValue;
                        }
                    } else {
                        Number numberValue = value.contains(".") ? Double.parseDouble(value) : Integer.parseInt(value);
                        numberRange.putIfAbsent(attributeName, new Number[2]);
                        if (key.startsWith("min")) {
                            numberRange.get(attributeName)[0] = numberValue;
                        } else if (key.startsWith("max")) {
                            numberRange.get(attributeName)[1] = numberValue;
                        }
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
        personSearchCriteriaDto.setNumberRange(numberRange);
        personSearchCriteriaDto.setDateRange(dateRange);

        Page<Person> personListPage = personRepository.findAll(PersonSpecification.createSpecification(personSearchCriteriaDto), pageable);

        List<PersonDto> personDtoList = personListPage.getContent().stream().map(p -> modelMapper.map(p, PersonDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(new PageImpl<>(personDtoList, pageable, personListPage.getTotalElements()));

    }

    @PutMapping()
    public ResponseEntity<PersonDto> updatePerson(@RequestBody UpdatePersonCommand updatePersonCommand) {
        Person person = personService.updatePerson(updatePersonCommand);

        PersonDto personDto = modelMapper.map(person, PersonDto.class);

        return ResponseEntity.status(HttpStatus.OK).body(personDto);
    }

    @PostMapping("/{personId}/employment")
    //TODO: Create DTO & Command, rething if we really need return whole employment history
    public ResponseEntity<List<EmploymentHistory>> createNewEmployment(
            @PathVariable Long personId,
            @RequestBody EmploymentHistory employmentHistory
    ) {
        Person person = employmentHistoryService.createNewEmployment(personId, employmentHistory);

        return ResponseEntity.status(HttpStatus.CREATED).body(person.getEmploymentHistory());
    }

}
