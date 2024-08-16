package pl.kurs.anonymoussurveillance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class AuthenticationResponseDto {
    private String message;
    private long timestamp;
    private int status;
    private HttpStatus error;
    private String path;
}
