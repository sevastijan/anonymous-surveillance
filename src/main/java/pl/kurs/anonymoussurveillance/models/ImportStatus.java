package pl.kurs.anonymoussurveillance.models;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public long getProcessedRows() {
        return processedRows;
    }

    public void setProcessedRows(long processedRows) {
        this.processedRows = processedRows;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public double getRowsPerSecond() {
        return rowsPerSecond;
    }

    public void setRowsPerSecond(double rowsPerSecond) {
        this.rowsPerSecond = rowsPerSecond;
    }
}
