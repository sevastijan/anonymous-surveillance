package pl.kurs.anonymoussurveillance.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kurs.anonymoussurveillance.commands.CreatePersonTypeCommand;
import pl.kurs.anonymoussurveillance.dto.PersonTypeDto;
import pl.kurs.anonymoussurveillance.models.PersonType;
import pl.kurs.anonymoussurveillance.services.PersonTypeService;

@RestController
@RequestMapping("/api/v1/person-type")
public class PersonTypeController {
    private PersonTypeService personTypeService;
    private ModelMapper modelMapper;

    public PersonTypeController(PersonTypeService personTypeService, ModelMapper modelMapper) {
        this.personTypeService = personTypeService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<PersonTypeDto> createPersonType(@RequestBody CreatePersonTypeCommand createPersonTypeCommand) {
        PersonType personType = modelMapper.map(createPersonTypeCommand, PersonType.class);

        personTypeService.savePersonType(personType);

        PersonTypeDto savedPersonTypeDto = modelMapper.map(personType, PersonTypeDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPersonTypeDto);
    }
}
