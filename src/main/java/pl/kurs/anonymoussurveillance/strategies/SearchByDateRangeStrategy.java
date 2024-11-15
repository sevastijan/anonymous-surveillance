package pl.kurs.anonymoussurveillance.strategies;

import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SearchByDateRangeStrategy implements SearchStrategy {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    public void process(String key, String value, PersonSearchCriteriaDto personSearchCriteriaDto) {
        String attributeName = key.substring(3);
        LocalDate dateValue = LocalDate.parse(value, dateTimeFormatter);

        personSearchCriteriaDto.getDateRange().putIfAbsent(attributeName, new LocalDate[2]);

        if(key.startsWith("min")) {
            personSearchCriteriaDto.getDateRange().get(attributeName)[0] = dateValue;
        } else if(key.startsWith("max")) {
            personSearchCriteriaDto.getDateRange().get(attributeName)[1] = dateValue;
        }
    }
}
