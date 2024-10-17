package pl.kurs.anonymoussurveillance.controllers;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.kurs.anonymoussurveillance.commands.CreateEmploymentCommand;
import pl.kurs.anonymoussurveillance.commands.UpdateEmploymentCommand;
import pl.kurs.anonymoussurveillance.commands.UpdatePersonCommand;
import pl.kurs.anonymoussurveillance.dto.*;
import pl.kurs.anonymoussurveillance.exceptions.EmploymentDateOverlapException;
import pl.kurs.anonymoussurveillance.exceptions.EmploymentNotFoundException;
import pl.kurs.anonymoussurveillance.exceptions.PersonNotFoundException;
import pl.kurs.anonymoussurveillance.models.Employment;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.repositories.PersonRepository;
import pl.kurs.anonymoussurveillance.services.EmploymentService;
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
    private final EmploymentService employmentService;

    @GetMapping
    @Transactional(readOnly = true)
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
    @Transactional
    public ResponseEntity<List<EmploymentDto>> createNewEmployment(
            @PathVariable Long personId,
            @RequestBody CreateEmploymentCommand createEmploymentCommand
    ) {
        Employment employment = modelMapper.map(createEmploymentCommand, Employment.class);
        Person person = employmentService.createNewEmployment(personId, employment);

        List<EmploymentDto> employmentDtoList = person.getEmployment().stream()
                .map(e -> modelMapper.map(e, EmploymentDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(employmentDtoList);
    }


    @PutMapping("/{personId}/employment/{employmentId}")
    @Transactional
    public ResponseEntity<EmploymentDto> updateEmployment(
            @PathVariable Long personId,
            @PathVariable Long employmentId,
            @RequestBody UpdateEmploymentCommand updateEmploymentCommand
    ) {
        Employment employment = modelMapper.map(updateEmploymentCommand, Employment.class);
        Employment updatedPosition = employmentService.updateEmploymentHistory(personId, employmentId, employment);
        EmploymentDto employmentDto = modelMapper.map(updatedPosition, EmploymentDto.class);

        return ResponseEntity.status(HttpStatus.OK).body(employmentDto);
    }

    @DeleteMapping("/{personId}/employment/{employmentId}")
    @Transactional
    public ResponseEntity<Void> deleteEmployment(
            @PathVariable Long personId,
            @PathVariable Long employmentId
    ) {
        employmentService.removeEmployment(personId, employmentId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<ErrorDto> handlePersonNotFoundException(PersonNotFoundException exception) {
        ErrorDto errorResponse = new ErrorDto(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(EmploymentNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEmploymentNotFoundException(EmploymentNotFoundException exception) {
        ErrorDto errorResponse = new ErrorDto(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(EmploymentDateOverlapException.class)
    public ResponseEntity<ErrorDto> handleEmploymentDateOverlapException(EmploymentDateOverlapException exception) {
        ErrorDto errorResponse = new ErrorDto(
                HttpStatus.CONFLICT.value(),
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorDto> handleOptimisticLockingFailureException(OptimisticLockingFailureException exception) {
        ErrorDto errorResponse = new ErrorDto(
                HttpStatus.LOCKED.value(),
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
    }

}
