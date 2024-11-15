package pl.kurs.anonymoussurveillance.strategies;

import pl.kurs.anonymoussurveillance.dto.PersonAttributeCriteriaDto;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;

public class SearchByAttributesStrategy implements SearchStrategy {
    @Override
    public void process(String key, String value, PersonSearchCriteriaDto personSearchCriteriaDto) {
        PersonAttributeCriteriaDto personAttributeCriteriaDto = new PersonAttributeCriteriaDto();
        personAttributeCriteriaDto.setName(key);
        personAttributeCriteriaDto.setValue(value);

        personSearchCriteriaDto.getAttributes().add(personAttributeCriteriaDto);
    }
}
