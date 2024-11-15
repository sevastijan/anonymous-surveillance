package pl.kurs.anonymoussurveillance.strategies;

import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;

public interface SearchStrategy {
    void process(String key, String value, PersonSearchCriteriaDto personSearchCriteriaDto);
}
