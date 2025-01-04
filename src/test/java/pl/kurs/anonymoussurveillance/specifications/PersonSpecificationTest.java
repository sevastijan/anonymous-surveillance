package pl.kurs.anonymoussurveillance.specifications;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import pl.kurs.anonymoussurveillance.dto.PersonAttributeCriteriaDto;
import pl.kurs.anonymoussurveillance.dto.PersonSearchCriteriaDto;
import pl.kurs.anonymoussurveillance.models.Person;
import pl.kurs.anonymoussurveillance.models.PersonAttribute;

import jakarta.persistence.criteria.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PersonSpecificationTest {

    @Test
    public void shouldReturnNullWhenNoCriteriaProvided() {
        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNull(predicate);
    }


    @Test
    public void shouldCreatePredicateWithAttributeNameAndValue() {
        PersonAttributeCriteriaDto attributeCriteria = new PersonAttributeCriteriaDto();
        attributeCriteria.setName("height");
        attributeCriteria.setValue("165");

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setAttributes(Collections.singletonList(attributeCriteria));

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        Root root = mock(Root.class);
        Subquery subquery = mock(Subquery.class);
        Root subRoot = mock(Root.class);

        Join<Person, PersonAttribute> attributesJoin = mock(String.valueOf(Join.class));
        Path<String> namePath = mock(String.valueOf(Path.class));
        Path<String> valuePath = mock(String.valueOf(Path.class));
        Path<Long> idPath = mock(String.valueOf(Path.class));
        Path<Long> subIdPath = mock(String.valueOf(Path.class));

        Predicate namePredicate = mock(Predicate.class);
        Predicate valuePredicate = mock(Predicate.class);
        Predicate idPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);
        Predicate existsPredicate = mock(Predicate.class);

        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Person.class)).thenReturn(subRoot);
        when(subRoot.<Person, PersonAttribute>join("attributes")).thenReturn(attributesJoin);
        when(root.<Long>get("id")).thenReturn(idPath);
        when(subRoot.<Long>get("id")).thenReturn(subIdPath);

        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);

        when(subquery.select(any(Expression.class))).thenReturn(subquery);


        when(builder.equal(namePath, "height")).thenReturn(namePredicate);
        when(builder.equal(subIdPath, idPath)).thenReturn(idPredicate);
        when(builder.equal(valuePath, "165")).thenReturn(valuePredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);
        when(builder.exists(subquery)).thenReturn(existsPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        verify(query).subquery(Long.class);
        verify(subquery).from(Person.class);
        verify(subquery).select(subIdPath);
        verify(builder).exists(subquery);
        verify(builder, atLeastOnce()).and(any(Predicate[].class));

    }

    @Test
    public void shouldCreatePredicateForNumberRange() {
        Map<String, Number[]> numberRange = new HashMap<>();
        numberRange.put("height", new Number[]{170, 180});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(numberRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        Root root = mock(Root.class);
        Subquery subquery = mock(Subquery.class);
        Root subRoot = mock(Root.class);
        Join attributesJoin = mock(Join.class);
        Path namePath = mock(Path.class);
        Path valuePath = mock(Path.class);
        Path idPath = mock(Path.class);
        Path subIdPath = mock(Path.class);
        Expression intPath = mock(Expression.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate maxPredicate = mock(Predicate.class);
        Predicate idPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);
        Predicate existsPredicate = mock(Predicate.class);

        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Person.class)).thenReturn(subRoot);
        when(subRoot.<Person, PersonAttribute>join("attributes")).thenReturn(attributesJoin);
        when(root.<Long>get("id")).thenReturn(idPath);
        when(subRoot.<Long>get("id")).thenReturn(subIdPath);
        when(subquery.select(any(Expression.class))).thenReturn(subquery);

        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);

        when(builder.equal(namePath, "height")).thenReturn(namePredicate);
        when(builder.equal(subIdPath, idPath)).thenReturn(idPredicate);
        when(builder.function(eq("CAST"), eq(Integer.class), eq(valuePath))).thenReturn(intPath);
        when(builder.greaterThanOrEqualTo(intPath, 170)).thenReturn(minPredicate);
        when(builder.lessThanOrEqualTo(intPath, 180)).thenReturn(maxPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);
        when(builder.exists(subquery)).thenReturn(existsPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        verify(query).subquery(Long.class);
        verify(subquery).from(Person.class);
        verify(subquery).select(subIdPath);
        verify(builder).exists(subquery);
        verify(builder).equal(namePath, "height");
        verify(builder).greaterThanOrEqualTo(intPath, 170);
        verify(builder).lessThanOrEqualTo(intPath, 180);
        verify(builder, atLeastOnce()).and(any(Predicate[].class));
    }

    @Test
    public void shouldCreatePredicateForDateRange() {
        Map<String, LocalDate[]> dateRange = new HashMap<>();
        dateRange.put("birthDate", new LocalDate[]{LocalDate.of(1988, 1, 1), null});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setDateRange(dateRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<Object> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);
        Subquery<Long> subquery = mock(Subquery.class);
        Root<Person> subRoot = mock(Root.class);
        Join<Person, PersonAttribute> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);
        Path<Long> subIdPath = mock(Path.class);
        Expression<LocalDate> datePath = mock(Expression.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate idPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);
        Predicate existsPredicate = mock(Predicate.class);

        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Person.class)).thenReturn(subRoot);
        when(subRoot.<Person, PersonAttribute>join("attributes")).thenReturn(attributesJoin);
        when(root.<Long>get("id")).thenReturn(idPath);
        when(subRoot.<Long>get("id")).thenReturn(subIdPath);
        when(subquery.select(any(Expression.class))).thenReturn(subquery);

        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);

        when(builder.equal(namePath, "birthDate")).thenReturn(namePredicate);
        when(builder.equal(subIdPath, idPath)).thenReturn(idPredicate);
        when(valuePath.as(LocalDate.class)).thenReturn(datePath);
        when(builder.greaterThanOrEqualTo(datePath, LocalDate.of(1988, 1, 1))).thenReturn(minPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);
        when(builder.exists(subquery)).thenReturn(existsPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        verify(query).subquery(Long.class);
        verify(subquery).from(Person.class);
        verify(subquery).select(subIdPath);
        verify(builder).exists(subquery);
        verify(builder).equal(namePath, "birthDate");
        verify(valuePath).as(LocalDate.class);
        verify(builder).greaterThanOrEqualTo(datePath, LocalDate.of(1988, 1, 1));
        verify(builder, atLeastOnce()).and(any(Predicate[].class));
    }

    @Test
    public void shouldCreateCombinedPredicateWhenAttributeNumberAndDateRangesAreProvided() {
        PersonAttributeCriteriaDto attributeCriteria = new PersonAttributeCriteriaDto();
        attributeCriteria.setName("height");
        attributeCriteria.setValue("180");

        Map<String, Number[]> numberRange = new HashMap<>();
        numberRange.put("height", new Number[]{175, 185});

        Map<String, LocalDate[]> dateRange = new HashMap<>();
        dateRange.put("birthDate", new LocalDate[]{LocalDate.of(1989, 1, 1), LocalDate.of(1991, 12, 31)});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setAttributes(Collections.singletonList(attributeCriteria));
        criteriaDto.setNumberRange(numberRange);
        criteriaDto.setDateRange(dateRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<Object> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);
        Subquery<Long> subquery = mock(Subquery.class);
        Root<Person> subRoot = mock(Root.class);

        Join<Person, PersonAttribute> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);
        Path<Long> subIdPath = mock(Path.class);
        Expression<Integer> intPath = mock(Expression.class);
        Expression<LocalDate> datePath = mock(Expression.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate valuePredicate = mock(Predicate.class);
        Predicate numberNamePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate maxPredicate = mock(Predicate.class);
        Predicate dateNamePredicate = mock(Predicate.class);
        Predicate dateMinPredicate = mock(Predicate.class);
        Predicate dateMaxPredicate = mock(Predicate.class);
        Predicate idPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);
        Predicate existsPredicate = mock(Predicate.class);

        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Person.class)).thenReturn(subRoot);
        when(subRoot.<Person, PersonAttribute>join("attributes")).thenReturn(attributesJoin);
        when(root.<Long>get("id")).thenReturn(idPath);
        when(subRoot.<Long>get("id")).thenReturn(subIdPath);
        when(subquery.select(any(Expression.class))).thenReturn(subquery);

        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);

        when(builder.equal(namePath, "height")).thenReturn(namePredicate);
        when(builder.equal(valuePath, "180")).thenReturn(valuePredicate);
        when(builder.equal(namePath, "height")).thenReturn(numberNamePredicate);
        when(builder.function(eq("CAST"), eq(Integer.class), eq(valuePath))).thenReturn(intPath);
        when(builder.greaterThanOrEqualTo(intPath, 175)).thenReturn(minPredicate);
        when(builder.lessThanOrEqualTo(intPath, 185)).thenReturn(maxPredicate);
        when(builder.equal(namePath, "birthDate")).thenReturn(dateNamePredicate);
        when(valuePath.as(LocalDate.class)).thenReturn(datePath);
        when(builder.greaterThanOrEqualTo(datePath, LocalDate.of(1989, 1, 1))).thenReturn(dateMinPredicate);
        when(builder.lessThanOrEqualTo(datePath, LocalDate.of(1991, 12, 31))).thenReturn(dateMaxPredicate);
        when(builder.equal(subIdPath, idPath)).thenReturn(idPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);
        when(builder.exists(subquery)).thenReturn(existsPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        verify(query, atLeast(3)).subquery(Long.class);
        verify(subquery, atLeast(3)).from(Person.class);
        verify(subquery, atLeast(3)).select(any(Expression.class));
        verify(builder, atLeast(3)).exists(subquery);
        verify(builder, atLeastOnce()).and(any(Predicate[].class));
    }

    @Test
    public void shouldCreatePredicateForDoubleRange() {
        Map<String, Number[]> numberRange = new HashMap<>();
        numberRange.put("weight", new Number[]{70.5, 80.0});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(numberRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<Object> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);
        Subquery<Long> subquery = mock(Subquery.class);
        Root<Person> subRoot = mock(Root.class);

        Join<Person, PersonAttribute> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);
        Path<Long> subIdPath = mock(Path.class);
        Expression<Double> doublePathMin = mock(Expression.class);
        Expression<Double> doublePathMax = mock(Expression.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate maxPredicate = mock(Predicate.class);
        Predicate idPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);
        Predicate existsPredicate = mock(Predicate.class);

        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Person.class)).thenReturn(subRoot);
        when(subRoot.<Person, PersonAttribute>join("attributes")).thenReturn(attributesJoin);
        when(root.<Long>get("id")).thenReturn(idPath);
        when(subRoot.<Long>get("id")).thenReturn(subIdPath);
        when(subquery.select(any(Expression.class))).thenReturn(subquery);

        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);

        when(builder.equal(namePath, "weight")).thenReturn(namePredicate);
        when(builder.equal(subIdPath, idPath)).thenReturn(idPredicate);
        when(builder.function(eq("CAST"), eq(Double.class), eq(valuePath)))
                .thenReturn(doublePathMin)
                .thenReturn(doublePathMax);
        when(builder.greaterThanOrEqualTo(doublePathMin, 70.5)).thenReturn(minPredicate);
        when(builder.lessThanOrEqualTo(doublePathMax, 80.0)).thenReturn(maxPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);
        when(builder.exists(subquery)).thenReturn(existsPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        verify(query).subquery(Long.class);
        verify(subquery).from(Person.class);
        verify(subquery).select(subIdPath);
        verify(builder).exists(subquery);
        verify(builder).equal(namePath, "weight");
        verify(builder, times(2)).function(eq("CAST"), eq(Double.class), eq(valuePath));
        verify(builder).greaterThanOrEqualTo(doublePathMin, 70.5);
        verify(builder).lessThanOrEqualTo(doublePathMax, 80.0);
        verify(builder, atLeastOnce()).and(any(Predicate[].class));
    }


    @Test
    public void shouldCreatePredicateForBigDecimalRange() {
        Map<String, Number[]> numberRange = new HashMap<>();
        numberRange.put("salary", new Number[]{new BigDecimal("50000.00"), new BigDecimal("100000.00")});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(numberRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<Object> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);
        Subquery<Long> subquery = mock(Subquery.class);
        Root<Person> subRoot = mock(Root.class);

        Join<Person, PersonAttribute> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);
        Path<Long> subIdPath = mock(Path.class);
        Expression<BigDecimal> bigDecimalPathMin = mock(Expression.class);
        Expression<BigDecimal> bigDecimalPathMax = mock(Expression.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate maxPredicate = mock(Predicate.class);
        Predicate idPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);
        Predicate existsPredicate = mock(Predicate.class);

        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Person.class)).thenReturn(subRoot);
        when(subRoot.<Person, PersonAttribute>join("attributes")).thenReturn(attributesJoin);
        when(root.<Long>get("id")).thenReturn(idPath);
        when(subRoot.<Long>get("id")).thenReturn(subIdPath);
        when(subquery.select(any(Expression.class))).thenReturn(subquery);

        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);

        when(builder.equal(namePath, "salary")).thenReturn(namePredicate);
        when(builder.equal(subIdPath, idPath)).thenReturn(idPredicate);
        when(builder.function(eq("CAST"), eq(BigDecimal.class), eq(valuePath)))
                .thenReturn(bigDecimalPathMin)
                .thenReturn(bigDecimalPathMax);
        when(builder.greaterThanOrEqualTo(bigDecimalPathMin, new BigDecimal("50000.00"))).thenReturn(minPredicate);
        when(builder.lessThanOrEqualTo(bigDecimalPathMax, new BigDecimal("100000.00"))).thenReturn(maxPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);
        when(builder.exists(subquery)).thenReturn(existsPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        verify(query).subquery(Long.class);
        verify(subquery).from(Person.class);
        verify(subquery).select(subIdPath);
        verify(builder).exists(subquery);
        verify(builder).equal(namePath, "salary");
        verify(builder, times(2)).function(eq("CAST"), eq(BigDecimal.class), eq(valuePath));
        verify(builder).greaterThanOrEqualTo(bigDecimalPathMin, new BigDecimal("50000.00"));
        verify(builder).lessThanOrEqualTo(bigDecimalPathMax, new BigDecimal("100000.00"));
        verify(builder, atLeastOnce()).and(any(Predicate[].class));
    }


    @Test
    public void shouldThrowExceptionWhenUnsupportedNumberTypeIsUsed() {
        Map<String, Number[]> numberRange = new HashMap<>();
        numberRange.put("unsupportedNumber", new Number[]{(short) 10, null});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(numberRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<Object> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);
        Subquery<Long> subquery = mock(Subquery.class);
        Root<Person> subRoot = mock(Root.class);

        Join<Person, PersonAttribute> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Path<Long> idPath = mock(Path.class);
        Path<Long> subIdPath = mock(Path.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate idPredicate = mock(Predicate.class);
        Predicate existsPredicate = mock(Predicate.class);

        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Person.class)).thenReturn(subRoot);
        when(subRoot.<Person, PersonAttribute>join("attributes")).thenReturn(attributesJoin);
        when(root.<Long>get("id")).thenReturn(idPath);
        when(subRoot.<Long>get("id")).thenReturn(subIdPath);
        when(subquery.select(any(Expression.class))).thenReturn(subquery);

        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);

        when(builder.equal(namePath, "unsupportedNumber")).thenReturn(namePredicate);
        when(builder.equal(subIdPath, idPath)).thenReturn(idPredicate);
        when(builder.exists(subquery)).thenReturn(existsPredicate);

        assertThrows(IllegalArgumentException.class, () ->
                specification.toPredicate(root, query, builder)
        );
    }
}
