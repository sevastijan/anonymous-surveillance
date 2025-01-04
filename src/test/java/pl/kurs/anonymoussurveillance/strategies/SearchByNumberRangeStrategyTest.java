package pl.kurs.anonymoussurveillance.strategies;

import org.junit.jupiter.api.Test;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class SearchByNumberRangeStrategyTest {

    @Test
    public void shouldProcessMinIntegerRange() {
        SearchByNumberRangeStrategy strategy = new SearchByNumberRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(new HashMap<>());
        String key = "minAge";
        String value = "18";

        strategy.process(key, value, criteriaDto);

        assertEquals(1, criteriaDto.getNumberRange().size());
        assertTrue(criteriaDto.getNumberRange().containsKey("Age"));
        assertEquals(18.0, criteriaDto.getNumberRange().get("Age")[0]);
        assertNull(criteriaDto.getNumberRange().get("Age")[1]);
    }

    @Test
    public void shouldProcessMaxDoubleRange() {
        SearchByNumberRangeStrategy strategy = new SearchByNumberRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(new HashMap<>());
        String key = "maxHeight";
        String value = "180.5";

        strategy.process(key, value, criteriaDto);

        assertEquals(1, criteriaDto.getNumberRange().size());
        assertTrue(criteriaDto.getNumberRange().containsKey("Height"));
        assertNull(criteriaDto.getNumberRange().get("Height")[0]);
        assertEquals(180.5, criteriaDto.getNumberRange().get("Height")[1]);
    }

    @Test
    public void shouldProcessBothMinAndMaxRange() {
        SearchByNumberRangeStrategy strategy = new SearchByNumberRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(new HashMap<>());

        strategy.process("minWeight", "70.5", criteriaDto);
        strategy.process("maxWeight", "90.5", criteriaDto);

        assertEquals(1, criteriaDto.getNumberRange().size());
        assertTrue(criteriaDto.getNumberRange().containsKey("Weight"));
        assertEquals(70.5, criteriaDto.getNumberRange().get("Weight")[0]);
        assertEquals(90.5, criteriaDto.getNumberRange().get("Weight")[1]);
    }

    @Test
    public void shouldThrowExceptionForInvalidNumberFormat() {
        SearchByNumberRangeStrategy strategy = new SearchByNumberRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(new HashMap<>());
        String key = "minAge";
        String value = "invalid";

        assertThrows(NumberFormatException.class, () ->
                strategy.process(key, value, criteriaDto)
        );
    }

    @Test
    public void shouldProcessIntegerWithDecimalPoint() {
        SearchByNumberRangeStrategy strategy = new SearchByNumberRangeStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(new HashMap<>());
        String key = "minAge";
        String value = "18.0";

        strategy.process(key, value, criteriaDto);

        assertEquals(1, criteriaDto.getNumberRange().size());
        assertTrue(criteriaDto.getNumberRange().containsKey("Age"));
        assertEquals(18.0, criteriaDto.getNumberRange().get("Age")[0]);
        assertNull(criteriaDto.getNumberRange().get("Age")[1]);
    }
}