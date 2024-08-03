package pl.kurs.anonymoussurveillance.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.pl.PESEL;
import pl.kurs.anonymoussurveillance.models.PersonType;

import java.util.Map;

@Getter
@Setter
public class CreatePersonCommand {
    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @PESEL
    private String pesel;

    @NotNull
    private int height;

    @NotNull
    private int weight;

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private PersonType personType;

    private Map<String, String> attributes;
}
