package pl.kurs.anonymoussurveillance.commands;

import lombok.*;
import pl.kurs.anonymoussurveillance.dto.PersonAttributeDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonCommand {
    private Long id;
    private Long version;
    private List<UpdatePersonAttributeCommand> attributes;
}
