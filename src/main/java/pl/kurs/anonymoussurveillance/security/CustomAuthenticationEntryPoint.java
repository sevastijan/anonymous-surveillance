package pl.kurs.anonymoussurveillance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pl.kurs.anonymoussurveillance.dto.AuthenticationResponseDto;
import pl.kurs.anonymoussurveillance.models.User;
import pl.kurs.anonymoussurveillance.repositories.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String username = (String) request.getAttribute("lastAttemptedUsername");
        String message;

        if (username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.getLockoutEndTime() != null && LocalDateTime.now().isBefore(user.getLockoutEndTime())) {
                message = "OH My lord, account is locked until " + user.getLockoutEndTime();
            } else {
                message = "My lord, invalid credentials";
            }
        } else {
            message = "My lord, Unauthorized access";
        }

        AuthenticationResponseDto authenticationResponseDto = new AuthenticationResponseDto(
                message,
                System.currentTimeMillis(),
                HttpServletResponse.SC_UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED,
                request.getServletPath()
        );

        objectMapper.writeValue(response.getOutputStream(), authenticationResponseDto);
    }
}
