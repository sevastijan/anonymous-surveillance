package pl.kurs.anonymoussurveillance.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.kurs.anonymoussurveillance.dto.PersonAttributeCriteriaDto;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersonSpecification {
    public static Specification<Person> createSpecification(PersonSearchCriteriaDto personSearchCriteriaDto) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (personSearchCriteriaDto.getAttributes() != null && !personSearchCriteriaDto.getAttributes().isEmpty()) {
                for (PersonAttributeCriteriaDto attributeCriteria : personSearchCriteriaDto.getAttributes()) {
                    Join<Person, PersonAttribute> attributesJoin = root.join("attributes");
                    if (attributeCriteria.getName() != null && !attributeCriteria.getName().isEmpty()) {
                        predicates.add(builder.equal(attributesJoin.get("name"), attributeCriteria.getName()));
                    }
                    if (attributeCriteria.getValue() != null && !attributeCriteria.getValue().isEmpty()) {
                        predicates.add(builder.like(builder.lower(attributesJoin.get("value")), "%" + attributeCriteria.getValue().toLowerCase() + "%"));
                    }
                }
            }

            if (personSearchCriteriaDto.getNumberRange() != null && !personSearchCriteriaDto.getNumberRange().isEmpty()) {
                for (Map.Entry<String, Number[]> entry : personSearchCriteriaDto.getNumberRange().entrySet()) {
                    String attributeName = entry.getKey();
                    Number[] range = entry.getValue();
                    Join<Person, PersonAttribute> attributesJoin = root.join("attributes");
                    Predicate attributeNamePredicate = builder.equal(attributesJoin.get("name"), attributeName);

                    if (range[0] != null) {
                        Predicate minPredicate = createNumericPredicate(builder, attributesJoin.get("value"), range[0], true);
                        predicates.add(builder.and(attributeNamePredicate, minPredicate));
                    }
                    if (range[1] != null) {
                        Predicate maxPredicate = createNumericPredicate(builder, attributesJoin.get("value"), range[1], false);
                        predicates.add(builder.and(attributeNamePredicate, maxPredicate));
                    }
                }
            }

            if (personSearchCriteriaDto.getDateRange() != null && !personSearchCriteriaDto.getDateRange().isEmpty()) {
                for (Map.Entry<String, LocalDate[]> entry : personSearchCriteriaDto.getDateRange().entrySet()) {
                    String attributeName = entry.getKey();
                    LocalDate[] range = entry.getValue();
                    Join<Person, PersonAttribute> attributesJoin = root.join("attributes");
                    Predicate attributeNamePredicate = builder.equal(attributesJoin.get("name"), attributeName);

                    if (range[0] != null) {
                        Predicate minPredicate = builder.greaterThanOrEqualTo(attributesJoin.get("value").as(LocalDate.class), range[0]);
                        predicates.add(builder.and(attributeNamePredicate, minPredicate));
                    }
                    if (range[1] != null) {
                        Predicate maxPredicate = builder.lessThanOrEqualTo(attributesJoin.get("value").as(LocalDate.class), range[1]);
                        predicates.add(builder.and(attributeNamePredicate, maxPredicate));
                    }
                }
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate createNumericPredicate(CriteriaBuilder builder, Expression<Number> path, Number value, boolean isMin) {
        if (value instanceof Integer) {
            Expression<Integer> intPath = builder.toInteger(path);
            return isMin ? builder.greaterThanOrEqualTo(intPath, (Integer) value)
                    : builder.lessThanOrEqualTo(intPath, (Integer) value);
        } else if (value instanceof Double) {
            Expression<Double> doublePath = builder.toDouble(path);
            return isMin ? builder.greaterThanOrEqualTo(doublePath, (Double) value)
                    : builder.lessThanOrEqualTo(doublePath, (Double) value);
        } else if (value instanceof BigDecimal) {
            Expression<BigDecimal> bigDecimalPath = builder.toBigDecimal(path);
            return isMin ? builder.greaterThanOrEqualTo(bigDecimalPath, (BigDecimal) value)
                    : builder.lessThanOrEqualTo(bigDecimalPath, (BigDecimal) value);
        } else {
            throw new IllegalArgumentException("Unsupported numeric type");
        }
    }
}
