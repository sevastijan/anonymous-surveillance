package pl.kurs.anonymoussurveillance.strategies;

import org.junit.jupiter.api.Test;
import pl.kurs.anonymoussurveillance.dto.PersonAttributeCriteriaDto;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SearchByAttributesStrategyTest {

    @Test
    public void shouldProcessAttributeSearchCriteria() {
        SearchByAttributesStrategy strategy = new SearchByAttributesStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setAttributes(new ArrayList<>());
        String key = "height";
        String value = "180";


        strategy.process(key, value, criteriaDto);


        assertEquals(1, criteriaDto.getAttributes().size());
        PersonAttributeCriteriaDto addedAttribute = criteriaDto.getAttributes().get(0);
        assertEquals(key, addedAttribute.getName());
        assertEquals(value, addedAttribute.getValue());
    }

    @Test
    public void shouldProcessMultipleAttributes() {
        SearchByAttributesStrategy strategy = new SearchByAttributesStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setAttributes(new ArrayList<>());

        String[][] attributes = {
                {"height", "180"},
                {"weight", "75"},
                {"eyeColor", "blue"}
        };

        for (String[] attribute : attributes) {
            strategy.process(attribute[0], attribute[1], criteriaDto);
        }

        assertEquals(3, criteriaDto.getAttributes().size());

        for (int i = 0; i < attributes.length; i++) {
            PersonAttributeCriteriaDto addedAttribute = criteriaDto.getAttributes().get(i);
            assertEquals(attributes[i][0], addedAttribute.getName());
            assertEquals(attributes[i][1], addedAttribute.getValue());
        }
    }

    @Test
    public void shouldProcessAttributeWithNullValue() {
        SearchByAttributesStrategy strategy = new SearchByAttributesStrategy();
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setAttributes(new ArrayList<>());
        String key = "height";
        String value = null;

        strategy.process(key, value, criteriaDto);

        assertEquals(1, criteriaDto.getAttributes().size());
        PersonAttributeCriteriaDto addedAttribute = criteriaDto.getAttributes().get(0);
        assertEquals(key, addedAttribute.getName());
        assertNull(addedAttribute.getValue());
    }
}