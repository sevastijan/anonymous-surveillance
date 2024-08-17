package pl.kurs.anonymoussurveillance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportDto {
    private Long id;
    private LocalDateTime importDateTime;
}
