package pl.kurs.anonymoussurveillance.commands;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;
import pl.kurs.anonymoussurveillance.models.RequiredAttribute;

import java.util.List;

@Getter
@Setter
public class  CreatePersonTypeCommand {
    @NotNull
    private String name;

    @NotNull
    private List<RequiredAttribute> attributes;

}
