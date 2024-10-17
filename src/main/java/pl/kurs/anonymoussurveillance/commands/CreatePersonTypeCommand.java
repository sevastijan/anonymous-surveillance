package pl.kurs.anonymoussurveillance.commands;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;
import pl.kurs.anonymoussurveillance.models.RequiredAttribute;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class  CreatePersonTypeCommand {
    @NotNull
    private String name;

    @NotNull
    private List<RequiredAttribute> attributes;

}
