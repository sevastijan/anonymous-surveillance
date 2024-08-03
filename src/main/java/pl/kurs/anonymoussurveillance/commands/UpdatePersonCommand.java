package pl.kurs.anonymoussurveillance.commands;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.kurs.anonymoussurveillance.dto.PersonAttributeDto;

import java.util.List;

@Getter
@Setter
public class UpdatePersonCommand {
    private Long id;
    private Long version;
    private List<UpdatePersonAttributeCommand> attributes;
}
