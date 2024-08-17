package pl.kurs.anonymoussurveillance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportStatusDto {
    private Long id;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long processedRows;
    private String errorMessage;
    private double rowsPerSecond;
}
