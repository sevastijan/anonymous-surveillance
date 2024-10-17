package pl.kurs.anonymoussurveillance.commands;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonAttributeCommand {
    private String name;
    private String value;
}
