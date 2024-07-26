package pl.kurs.anonymoussurveillance.specifications;

import org.springframework.data.jpa.domain.Specification;
import pl.kurs.anonymoussurveillance.models.Person;

import java.math.BigDecimal;

public class PersonSpecifications {
    public static Specification<Person> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
    }

    public static Specification<Person> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
    }

    public static Specification<Person> hasPesel(String pesel) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("pesel"), pesel);
    }

    public static Specification<Person> heightBetween(Integer minHeight, Integer maxHeight) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("height"), minHeight, maxHeight);
    }

    public static Specification<Person> weightBetween(Integer minWeight, Integer maxWeight) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("weight"), minWeight, maxWeight);
    }

    public static Specification<Person> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    //TODO: dodać wiecej specyfikacji
    public static Specification<Person> universityContains(String university) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);  // avoid duplicate rows in results
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("university")), "%" + university.toLowerCase() + "%");
        };
    }

    public static Specification<Person> salaryBetween(BigDecimal minSalary, BigDecimal maxSalary) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("salary"), minSalary, maxSalary);
    }

}
