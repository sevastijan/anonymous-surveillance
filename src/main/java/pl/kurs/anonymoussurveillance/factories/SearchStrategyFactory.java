package pl.kurs.anonymoussurveillance.factories;

import pl.kurs.anonymoussurveillance.strategies.SearchByAttributesStrategy;
import pl.kurs.anonymoussurveillance.strategies.SearchByDateRangeStrategy;
import pl.kurs.anonymoussurveillance.strategies.SearchByNumberRangeStrategy;
import pl.kurs.anonymoussurveillance.strategies.SearchStrategy;

public class SearchStrategyFactory {

    public static SearchStrategy getStrategy(String key) {
        if(key.startsWith("min") || key.startsWith("max")) {
            if(key.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return new SearchByDateRangeStrategy();
            } else {
                return new SearchByNumberRangeStrategy();
            }
        } else {
            return new SearchByAttributesStrategy();
        }
    }
}
