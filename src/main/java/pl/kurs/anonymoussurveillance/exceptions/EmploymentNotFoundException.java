package pl.kurs.anonymoussurveillance.exceptions;

public class EmploymentNotFoundException extends RuntimeException {
    public EmploymentNotFoundException(Long employmentId, Long personId) {
        super("Employment with id: " + employmentId + " and person id: " + personId + " not found.");

    }
}
