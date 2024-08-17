package pl.kurs.anonymoussurveillance.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorDto {
    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorDto(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
