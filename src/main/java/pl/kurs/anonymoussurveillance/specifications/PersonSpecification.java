package pl.kurs.anonymoussurveillance.specifications;

import jakarta.persistence.criteria.*;
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
            if (query.getResultType().equals(Long.class)) {
                return createPredicates(root, query, builder, personSearchCriteriaDto);
            }

//            query.distinct(true);
            return createPredicates(root, query, builder, personSearchCriteriaDto);
        };
    }
    private static Predicate createPredicates(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder builder, PersonSearchCriteriaDto personSearchCriteriaDto) {
        List<Predicate> predicates = new ArrayList<>();

        if (personSearchCriteriaDto.getAttributes() != null && !personSearchCriteriaDto.getAttributes().isEmpty()) {
            List<Predicate> attributePredicates = new ArrayList<>();

            for (PersonAttributeCriteriaDto attributeCriteria : personSearchCriteriaDto.getAttributes()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Person> subRoot = subquery.from(Person.class);
                Join<Person, PersonAttribute> attributesJoin = subRoot.join("attributes");

                List<Predicate> subPredicates = new ArrayList<>();
                subPredicates.add(builder.equal(subRoot.get("id"), root.get("id")));

                if (attributeCriteria.getName() != null && !attributeCriteria.getName().isEmpty()) {
                    subPredicates.add(builder.equal(attributesJoin.get("name"), attributeCriteria.getName()));
                }
                if (attributeCriteria.getValue() != null && !attributeCriteria.getValue().isEmpty()) {
                    subPredicates.add(builder.equal(attributesJoin.get("value"), attributeCriteria.getValue()));
                }

                subquery.select(subRoot.get("id"))
                        .where(builder.and(subPredicates.toArray(new Predicate[0])));

                attributePredicates.add(builder.exists(subquery));
            }

            if (!attributePredicates.isEmpty()) {
                predicates.add(builder.and(attributePredicates.toArray(new Predicate[0])));
            }
        }

        if (personSearchCriteriaDto.getNumberRange() != null && !personSearchCriteriaDto.getNumberRange().isEmpty()) {
            for (Map.Entry<String, Number[]> entry : personSearchCriteriaDto.getNumberRange().entrySet()) {
                String attributeName = entry.getKey();
                Number[] range = entry.getValue();

                if (range[0] != null || range[1] != null) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<Person> subRoot = subquery.from(Person.class);
                    Join<Person, PersonAttribute> attributesJoin = subRoot.join("attributes");

                    List<Predicate> subPredicates = new ArrayList<>();
                    subPredicates.add(builder.equal(subRoot.get("id"), root.get("id")));
                    subPredicates.add(builder.equal(attributesJoin.get("name"), attributeName));

                    if (range[0] != null) {
                        subPredicates.add(createNumericPredicate(builder, attributesJoin.get("value"), range[0], true));
                    }
                    if (range[1] != null) {
                        subPredicates.add(createNumericPredicate(builder, attributesJoin.get("value"), range[1], false));
                    }

                    subquery.select(subRoot.get("id"))
                            .where(builder.and(subPredicates.toArray(new Predicate[0])));

                    predicates.add(builder.exists(subquery));
                }
            }
        }

        if (personSearchCriteriaDto.getDateRange() != null && !personSearchCriteriaDto.getDateRange().isEmpty()) {
            for (Map.Entry<String, LocalDate[]> entry : personSearchCriteriaDto.getDateRange().entrySet()) {
                String attributeName = entry.getKey();
                LocalDate[] range = entry.getValue();

                if (range[0] != null || range[1] != null) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<Person> subRoot = subquery.from(Person.class);
                    Join<Person, PersonAttribute> attributesJoin = subRoot.join("attributes");

                    List<Predicate> subPredicates = new ArrayList<>();
                    subPredicates.add(builder.equal(subRoot.get("id"), root.get("id")));
                    subPredicates.add(builder.equal(attributesJoin.get("name"), attributeName));

                    Expression<LocalDate> datePath = attributesJoin.get("value").as(LocalDate.class);

                    if (range[0] != null) {
                        subPredicates.add(builder.greaterThanOrEqualTo(datePath, range[0]));
                    }
                    if (range[1] != null) {
                        subPredicates.add(builder.lessThanOrEqualTo(datePath, range[1]));
                    }

                    subquery.select(subRoot.get("id"))
                            .where(builder.and(subPredicates.toArray(new Predicate[0])));

                    predicates.add(builder.exists(subquery));
                }
            }
        }

        if (predicates.isEmpty()) {
            return builder.conjunction();
        } else {
            return builder.and(predicates.toArray(new Predicate[0]));
        }
    }

    private static Predicate createNumericPredicate(CriteriaBuilder builder, Expression<String> path, Number value, boolean isMin) {
        if (value instanceof Integer) {
            Expression<Integer> intPath = builder.function("CAST", Integer.class, path);
            return isMin ? builder.greaterThanOrEqualTo(intPath, (Integer) value)
                    : builder.lessThanOrEqualTo(intPath, (Integer) value);
        } else if (value instanceof Double) {
            Expression<Double> doublePath = builder.function("CAST", Double.class, path);
            return isMin ? builder.greaterThanOrEqualTo(doublePath, (Double) value)
                    : builder.lessThanOrEqualTo(doublePath, (Double) value);
        } else if (value instanceof BigDecimal) {
            Expression<BigDecimal> bigDecimalPath = builder.function("CAST", BigDecimal.class, path);
            return isMin ? builder.greaterThanOrEqualTo(bigDecimalPath, (BigDecimal) value)
                    : builder.lessThanOrEqualTo(bigDecimalPath, (BigDecimal) value);
        } else {
            throw new IllegalArgumentException("Unsupported numeric type");
        }
    }

}
