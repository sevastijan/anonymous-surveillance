package pl.kurs.anonymoussurveillance.specifications;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);
        Join<?, ?> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate valuePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.join("attributes")).thenReturn((Join<Object, Object>) attributesJoin);
        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);

        when(builder.equal(namePath, "height")).thenReturn(namePredicate);
        when(builder.lower(valuePath)).thenReturn(valuePath);
        when(builder.like(valuePath, "%165%")).thenReturn(valuePredicate);

        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        assertEquals(combinedPredicate, predicate);

        verify(builder).equal(namePath, "height");
        verify(builder).lower(valuePath);
        verify(builder).like(valuePath, "%165%");

        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicateCaptor.capture());

        Predicate[] capturedPredicates = predicateCaptor.getValue();

        assertEquals(2, capturedPredicates.length);
        assertTrue(Arrays.asList(capturedPredicates).contains(namePredicate));
        assertTrue(Arrays.asList(capturedPredicates).contains(valuePredicate));
    }

    @Test
    public void shouldCreatePredicateForNumberRange() {
        Map<String, Number[]> numberRange = new HashMap<>();
        numberRange.put("height", new Number[]{170, null});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(numberRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);

        Join<?, ?> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Expression<Integer> intPath = mock(Expression.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.join("attributes")).thenReturn((Join<Object, Object>) attributesJoin);
        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);
        when(builder.equal(namePath, "height")).thenReturn(namePredicate);
        when(builder.function(eq("CAST"), eq(Integer.class), eq(valuePath))).thenReturn(intPath);
        when(builder.greaterThanOrEqualTo(intPath, 170)).thenReturn(minPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);


        assertNotNull(predicate);
        assertEquals(combinedPredicate, predicate);

        verify(builder).equal(namePath, "height");
        verify(builder).function(eq("CAST"), eq(Integer.class), eq(valuePath));
        verify(builder).greaterThanOrEqualTo(intPath, 170);


        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicateCaptor.capture());

        Predicate[] capturedPredicates = predicateCaptor.getValue();

        assertEquals(2, capturedPredicates.length);
        assertTrue(Arrays.asList(capturedPredicates).contains(namePredicate));
        assertTrue(Arrays.asList(capturedPredicates).contains(minPredicate));
    }

    @Test
    public void shouldCreatePredicateForDateRange() {
        Map<String, LocalDate[]> dateRange = new HashMap<>();
        dateRange.put("birthDate", new LocalDate[]{LocalDate.of(1988, 1, 1), null}); // birthDate >= 1988-01-01

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setDateRange(dateRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);

        Join<?, ?> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Expression<LocalDate> datePath = mock(Expression.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.join("attributes")).thenReturn((Join<Object, Object>) attributesJoin);
        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);
        when(builder.equal(namePath, "birthDate")).thenReturn(namePredicate);

        when(valuePath.as(LocalDate.class)).thenReturn(datePath);
        when(builder.greaterThanOrEqualTo(datePath, LocalDate.of(1988, 1, 1))).thenReturn(minPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        assertEquals(combinedPredicate, predicate);

        verify(builder).equal(namePath, "birthDate");
        verify(valuePath).as(LocalDate.class);
        verify(builder).greaterThanOrEqualTo(datePath, LocalDate.of(1988, 1, 1));

        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicateCaptor.capture());

        Predicate[] capturedPredicates = predicateCaptor.getValue();

        assertEquals(2, capturedPredicates.length);
        assertTrue(Arrays.asList(capturedPredicates).contains(namePredicate));
        assertTrue(Arrays.asList(capturedPredicates).contains(minPredicate));
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
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);

        Join<?, ?> attributesJoin1 = mock(Join.class);
        Path<String> namePath1 = mock(Path.class);
        Path<String> valuePath1 = mock(Path.class);
        Predicate namePredicate1 = mock(Predicate.class);
        Predicate valuePredicate1 = mock(Predicate.class);

        Join<?, ?> attributesJoin2 = mock(Join.class);
        Path<String> namePath2 = mock(Path.class);
        Path<String> valuePath2 = mock(Path.class);
        Expression<Integer> intPath = mock(Expression.class);
        Predicate namePredicate2 = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate maxPredicate = mock(Predicate.class);

        Join<?, ?> attributesJoin3 = mock(Join.class);
        Path<String> namePath3 = mock(Path.class);
        Path<String> valuePath3 = mock(Path.class);
        Expression<LocalDate> datePath = mock(Expression.class);
        Predicate namePredicate3 = mock(Predicate.class);
        Predicate dateMinPredicate = mock(Predicate.class);
        Predicate dateMaxPredicate = mock(Predicate.class);

        when(root.join("attributes"))
                .thenReturn((Join<Object, Object>) attributesJoin1)
                .thenReturn((Join<Object, Object>) attributesJoin2)
                .thenReturn((Join<Object, Object>) attributesJoin3);
        when(attributesJoin1.<String>get("name")).thenReturn(namePath1);
        when(attributesJoin1.<String>get("value")).thenReturn(valuePath1);
        when(builder.equal(namePath1, "height")).thenReturn(namePredicate1);
        when(builder.lower(valuePath1)).thenReturn(valuePath1);
        when(builder.like(valuePath1, "%180%")).thenReturn(valuePredicate1);
        when(attributesJoin2.<String>get("name")).thenReturn(namePath2);
        when(attributesJoin2.<String>get("value")).thenReturn(valuePath2);
        when(builder.equal(namePath2, "height")).thenReturn(namePredicate2);
        when(builder.function(eq("CAST"), eq(Integer.class), eq(valuePath2))).thenReturn(intPath);
        when(builder.greaterThanOrEqualTo(intPath, 175)).thenReturn(minPredicate);
        when(builder.lessThanOrEqualTo(intPath, 185)).thenReturn(maxPredicate);
        when(attributesJoin3.<String>get("name")).thenReturn(namePath3);
        when(attributesJoin3.<String>get("value")).thenReturn(valuePath3);
        when(builder.equal(namePath3, "birthDate")).thenReturn(namePredicate3);
        when(valuePath3.as(LocalDate.class)).thenReturn(datePath);
        when(builder.greaterThanOrEqualTo(datePath, LocalDate.of(1989, 1, 1))).thenReturn(dateMinPredicate);
        when(builder.lessThanOrEqualTo(datePath, LocalDate.of(1991, 12, 31))).thenReturn(dateMaxPredicate);

        Predicate finalPredicate = mock(Predicate.class);
        when(builder.and(any(Predicate[].class))).thenReturn(finalPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        assertEquals(finalPredicate, predicate);

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
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);

        Join<?, ?> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);

        Expression<Double> doublePathMin = mock(Expression.class);
        Expression<Double> doublePathMax = mock(Expression.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate maxPredicate = mock(Predicate.class);

        when(root.join("attributes")).thenReturn((Join<Object, Object>) attributesJoin);
        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);
        when(builder.equal(namePath, "weight")).thenReturn(namePredicate);
        when(builder.function(eq("CAST"), eq(Double.class), eq(valuePath)))
                .thenReturn(doublePathMin)
                .thenReturn(doublePathMax);
        when(builder.greaterThanOrEqualTo(doublePathMin, 70.5)).thenReturn(minPredicate);
        when(builder.lessThanOrEqualTo(doublePathMax, 80.0)).thenReturn(maxPredicate);

        Predicate combinedPredicate = mock(Predicate.class);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        assertEquals(combinedPredicate, predicate);

        verify(builder).equal(namePath, "weight");
        verify(builder, times(2)).function(eq("CAST"), eq(Double.class), eq(valuePath));
        verify(builder).greaterThanOrEqualTo(doublePathMin, 70.5);
        verify(builder).lessThanOrEqualTo(doublePathMax, 80.0);

        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicateCaptor.capture());

        Predicate[] capturedPredicates = predicateCaptor.getValue();

        assertEquals(3, capturedPredicates.length);
        assertTrue(Arrays.asList(capturedPredicates).contains(namePredicate));
        assertTrue(Arrays.asList(capturedPredicates).contains(minPredicate));
        assertTrue(Arrays.asList(capturedPredicates).contains(maxPredicate));
    }


    @Test
    public void shouldCreatePredicateForBigDecimalRange() {
        Map<String, Number[]> numberRange = new HashMap<>();
        numberRange.put("salary", new Number[]{new BigDecimal("50000.00"), new BigDecimal("100000.00")});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(numberRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);

        Join<?, ?> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);

        Expression<BigDecimal> bigDecimalPathMin = mock(Expression.class);
        Expression<BigDecimal> bigDecimalPathMax = mock(Expression.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate maxPredicate = mock(Predicate.class);

        when(root.join("attributes")).thenReturn((Join<Object, Object>) attributesJoin);
        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);
        when(builder.equal(namePath, "salary")).thenReturn(namePredicate);
        when(builder.function(eq("CAST"), eq(BigDecimal.class), eq(valuePath)))
                .thenReturn(bigDecimalPathMin)
                .thenReturn(bigDecimalPathMax);
        when(builder.greaterThanOrEqualTo(bigDecimalPathMin, new BigDecimal("50000.00"))).thenReturn(minPredicate);
        when(builder.lessThanOrEqualTo(bigDecimalPathMax, new BigDecimal("100000.00"))).thenReturn(maxPredicate);

        Predicate combinedPredicate = mock(Predicate.class);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Predicate predicate = specification.toPredicate(root, query, builder);

        assertNotNull(predicate);
        assertEquals(combinedPredicate, predicate);

        verify(builder).equal(namePath, "salary");
        verify(builder, times(2)).function(eq("CAST"), eq(BigDecimal.class), eq(valuePath));
        verify(builder).greaterThanOrEqualTo(bigDecimalPathMin, new BigDecimal("50000.00"));
        verify(builder).lessThanOrEqualTo(bigDecimalPathMax, new BigDecimal("100000.00"));

        ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicateCaptor.capture());

        Predicate[] capturedPredicates = predicateCaptor.getValue();

        assertEquals(3, capturedPredicates.length);
        assertTrue(Arrays.asList(capturedPredicates).contains(namePredicate));
        assertTrue(Arrays.asList(capturedPredicates).contains(minPredicate));
        assertTrue(Arrays.asList(capturedPredicates).contains(maxPredicate));
    }


    @Test
    public void shouldThrowExceptionWhenUnsupportedNumberTypeIsUsed() {
        Map<String, Number[]> numberRange = new HashMap<>();
        numberRange.put("unsupportedNumber", new Number[]{(short) 10, null});

        PersonSearchCriteriaDto criteriaDto = new PersonSearchCriteriaDto();
        criteriaDto.setNumberRange(numberRange);

        Specification<Person> specification = PersonSpecification.createSpecification(criteriaDto);

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Person> root = mock(Root.class);

        Join<?, ?> attributesJoin = mock(Join.class);
        Path<String> namePath = mock(Path.class);
        Path<String> valuePath = mock(Path.class);
        Predicate namePredicate = mock(Predicate.class);

        when(root.join("attributes")).thenReturn((Join<Object, Object>) attributesJoin);
        when(attributesJoin.<String>get("name")).thenReturn(namePath);
        when(attributesJoin.<String>get("value")).thenReturn(valuePath);
        when(builder.equal(namePath, "unsupportedNumber")).thenReturn(namePredicate);

        assertThrows(IllegalArgumentException.class, () -> {
            specification.toPredicate(root, query, builder);
        });
    }




}
