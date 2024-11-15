package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "import_status")
public class ImportStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long processedRows;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    private double rowsPerSecond;

    public ImportStatus(Status status, LocalDateTime createdDate, LocalDateTime startDate, LocalDateTime endDate, long processedRows, String errorMessage, double rowsPerSecond) {
        this.status = status;
        this.createdDate = createdDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.processedRows = processedRows;
        this.errorMessage = errorMessage;
        this.rowsPerSecond = rowsPerSecond;
    }

    @Override
    public String toString() {
        return "ImportStatus{" +
                "id=" + id +
                ", status=" + status +
                ", createdDate=" + createdDate +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", processedRows=" + processedRows +
                ", errorMessage='" + errorMessage + '\'' +
                ", rowsPerSecond=" + rowsPerSecond +
                '}';
    }
}
