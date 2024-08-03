package pl.kurs.anonymoussurveillance.commands;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePersonAttributeCommand {
    private String name;
    private String value;
}
