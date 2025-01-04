package pl.kurs.anonymoussurveillance.strategies;

import org.junit.jupiter.api.Test;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class SearchByDateRangeStrategyTest {

    @Test
    public void shouldProcessMinDateRange() {
        SearchByDateRangeStrategy strategy = new SearchByDateRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setDateRange(new HashMap<>());
        String key = "minBirthDate";
        String value = "2000-01-01";

        strategy.process(key, value, criteriaDto);

        assertEquals(1, criteriaDto.getDateRange().size());
        assertTrue(criteriaDto.getDateRange().containsKey("BirthDate"));
        assertEquals(
                LocalDate.of(2000, 1, 1),
                criteriaDto.getDateRange().get("BirthDate")[0]
        );
        assertNull(criteriaDto.getDateRange().get("BirthDate")[1]);
    }

    @Test
    public void shouldProcessMaxDateRange() {
        SearchByDateRangeStrategy strategy = new SearchByDateRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setDateRange(new HashMap<>());
        String key = "maxBirthDate";
        String value = "2000-12-31";

        strategy.process(key, value, criteriaDto);

        assertEquals(1, criteriaDto.getDateRange().size());
        assertTrue(criteriaDto.getDateRange().containsKey("BirthDate"));
        assertNull(criteriaDto.getDateRange().get("BirthDate")[0]);
        assertEquals(
                LocalDate.of(2000, 12, 31),
                criteriaDto.getDateRange().get("BirthDate")[1]
        );
    }

    @Test
    public void shouldProcessBothMinAndMaxDateRange() {
        SearchByDateRangeStrategy strategy = new SearchByDateRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setDateRange(new HashMap<>());

        strategy.process("minBirthDate", "2000-01-01", criteriaDto);
        strategy.process("maxBirthDate", "2000-12-31", criteriaDto);

        assertEquals(1, criteriaDto.getDateRange().size());
        assertTrue(criteriaDto.getDateRange().containsKey("BirthDate"));
        assertEquals(
                LocalDate.of(2000, 1, 1),
                criteriaDto.getDateRange().get("BirthDate")[0]
        );
        assertEquals(
                LocalDate.of(2000, 12, 31),
                criteriaDto.getDateRange().get("BirthDate")[1]
        );
    }

    @Test
    public void shouldThrowExceptionForInvalidDateFormat() {
        SearchByDateRangeStrategy strategy = new SearchByDateRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setDateRange(new HashMap<>());
        String key = "minBirthDate";
        String value = "2000/01/01";

        assertThrows(DateTimeParseException.class, () ->
                strategy.process(key, value, criteriaDto)
        );
    }
}