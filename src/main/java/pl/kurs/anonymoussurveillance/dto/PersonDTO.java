package pl.kurs.anonymoussurveillance.dto;

import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import java.util.List;

public class PersonDTO {
    private String name;
    private String surname;
    private String pesel;
    private int height;
    private int weight;
    private String email;
    private List<PersonAttribute> attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<PersonAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PersonAttribute> attributes) {
        this.attributes = attributes;
    }
}
