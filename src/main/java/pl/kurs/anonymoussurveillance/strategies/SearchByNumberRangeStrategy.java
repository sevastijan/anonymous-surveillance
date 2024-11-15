package pl.kurs.anonymoussurveillance.strategies;

import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;

public class SearchByNumberRangeStrategy implements SearchStrategy {
    @Override
    public void process(String key, String value, PersonSearchCriteriaDto personSearchCriteriaDto) {
        String attributeName = key.substring(3);
        Number numberValue = value.contains(".") ? Double.parseDouble(value) : Integer.parseInt(value);

        personSearchCriteriaDto.getNumberRange().putIfAbsent(attributeName, new Number[2]);

        if(key.startsWith("min")) {
            personSearchCriteriaDto.getNumberRange().get(attributeName)[0] = numberValue;
        } else if(key.startsWith("max")) {
            personSearchCriteriaDto.getNumberRange().get(attributeName)[1] = numberValue;
        }
    }
}
