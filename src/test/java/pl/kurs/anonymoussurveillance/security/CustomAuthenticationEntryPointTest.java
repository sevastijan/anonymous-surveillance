package pl.kurs.anonymoussurveillance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import pl.kurs.anonymoussurveillance.dto.AuthenticationDto;
import pl.kurs.anonymoussurveillance.models.User;
import pl.kurs.anonymoussurveillance.repositories.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @Captor
    private ArgumentCaptor<AuthenticationDto> authenticationDtoCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        customAuthenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper, userRepository);
    }

    @Test
    public void shouldReturnUnauthorizedAccessWhenUsernameIsNull() throws IOException {
        when(request.getAttribute("lastAttemptedUsername")).thenReturn(null);
        when(request.getServletPath()).thenReturn("/test-path");

        customAuthenticationEntryPoint.commence(request, response, authException);

        verify(objectMapper).writeValue(eq(response.getOutputStream()), authenticationDtoCaptor.capture());

        AuthenticationDto actualDto = authenticationDtoCaptor.getValue();

        assertEquals("My lord, Unauthorized access", actualDto.getMessage());
        assertEquals("/test-path", actualDto.getPath());
    }

    @Test
    public void shouldReturnInvalidCredentialsWhenUserNotFound() throws IOException {
        String username = "unknownUser";
        when(request.getAttribute("lastAttemptedUsername")).thenReturn(username);
        when(request.getServletPath()).thenReturn("/test-path");
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        customAuthenticationEntryPoint.commence(request, response, authException);

        verify(objectMapper).writeValue(eq(response.getOutputStream()), authenticationDtoCaptor.capture());

        AuthenticationDto actualDto = authenticationDtoCaptor.getValue();

        assertEquals("My lord, invalid credentials", actualDto.getMessage());
        assertEquals("/test-path", actualDto.getPath());
    }

    @Test
    public void shouldReturnInvalidCredentialsWhenUserIsNotLocked() throws IOException {
        String username = "validUser";
        when(request.getAttribute("lastAttemptedUsername")).thenReturn(username);
        when(request.getServletPath()).thenReturn("/test-path");

        User user = new User();
        user.setUsername(username);
        user.setLockoutEndTime(null);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        customAuthenticationEntryPoint.commence(request, response, authException);

        verify(objectMapper).writeValue(eq(response.getOutputStream()), authenticationDtoCaptor.capture());
        AuthenticationDto actualDto = authenticationDtoCaptor.getValue();

        assertEquals("My lord, invalid credentials", actualDto.getMessage());
        assertEquals("/test-path", actualDto.getPath());
    }

    @Test
    public void shouldReturnAccountLockedWhenUserIsLocked() throws IOException {
        String username = "lockedUser";
        LocalDateTime lockoutEndTime = LocalDateTime.now().plusMinutes(10);
        when(request.getAttribute("lastAttemptedUsername")).thenReturn(username);
        when(request.getServletPath()).thenReturn("/test-path");

        User user = new User();
        user.setUsername(username);
        user.setLockoutEndTime(lockoutEndTime);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        customAuthenticationEntryPoint.commence(request, response, authException);

        verify(objectMapper).writeValue(eq(response.getOutputStream()), authenticationDtoCaptor.capture());
        AuthenticationDto actualDto = authenticationDtoCaptor.getValue();

        String expectedMessage = "OH My lord, account is locked until " + lockoutEndTime;

        assertEquals(expectedMessage, actualDto.getMessage());
        assertEquals("/test-path", actualDto.getPath());
    }

    @Test
    public void shouldReturnInvalidCredentialsWhenUserIsLockedButLockoutTimePassed() throws IOException {
        String username = "unlockedUser";
        LocalDateTime lockoutEndTime = LocalDateTime.now().minusMinutes(10); // Czas blokady minął
        when(request.getAttribute("lastAttemptedUsername")).thenReturn(username);
        when(request.getServletPath()).thenReturn("/test-path");

        User user = new User();
        user.setUsername(username);
        user.setLockoutEndTime(lockoutEndTime);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        customAuthenticationEntryPoint.commence(request, response, authException);

        verify(objectMapper).writeValue(eq(response.getOutputStream()), authenticationDtoCaptor.capture());
        AuthenticationDto actualDto = authenticationDtoCaptor.getValue();

        assertEquals("My lord, invalid credentials", actualDto.getMessage());
        assertEquals("/test-path", actualDto.getPath());
    }
}
