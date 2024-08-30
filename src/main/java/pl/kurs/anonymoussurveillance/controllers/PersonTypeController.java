package pl.kurs.anonymoussurveillance.controllers;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.kurs.anonymoussurveillance.commands.CreatePersonTypeCommand;
import pl.kurs.anonymoussurveillance.dto.ErrorDto;
import pl.kurs.anonymoussurveillance.dto.PersonTypeDto;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.models.RequiredAttribute;
import pl.kurs.anonymoussurveillance.services.PersonTypeService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/person-type")
public class PersonTypeController {
    private final PersonTypeService personTypeService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<PersonTypeDto> createPersonType(@RequestBody CreatePersonTypeCommand createPersonTypeCommand) {
        PersonType personType = modelMapper.map(createPersonTypeCommand, PersonType.class);

        List<RequiredAttribute> requiredAttributes = createPersonTypeCommand.getAttributes().stream()
                .map(attr -> modelMapper.map(attr, RequiredAttribute.class))
                .collect(Collectors.toList());

        personType.setRequiredAttributes(requiredAttributes);

        if(personType.getRequiredAttributes() == null || personType.getRequiredAttributes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required attributes are missing");
        }

        personTypeService.savePersonType(personType);

        PersonTypeDto savedPersonTypeDto = modelMapper.map(personType, PersonTypeDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPersonTypeDto);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDto> handleResponseStatusException(ResponseStatusException exception) {
        ErrorDto errorResponse = new ErrorDto(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
