package pl.kurs.anonymoussurveillance.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.kurs.anonymoussurveillance.listeners.AuthenticationListener;
import pl.kurs.anonymoussurveillance.models.User;
import pl.kurs.anonymoussurveillance.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationListenerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationListener authenticationListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldIncrementFailedAttemptsOnFailedLoginWhenUserNotLocked() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setFailedLoginAttempts(1);
        user.setLockoutEndTime(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(authentication, new BadCredentialsException("Bad credentials"));

        authenticationListener.onApplicationEvent(event);

        assertEquals(2, user.getFailedLoginAttempts());
    }

    @Test
    public void shouldLockUserAfterMaxFailedAttempts() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setFailedLoginAttempts(2);
        user.setLockoutEndTime(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(authentication, new BadCredentialsException("Bad credentials"));

        authenticationListener.onApplicationEvent(event);

        assertEquals(0, user.getFailedLoginAttempts());
        assertNotNull(user.getLockoutEndTime());
        assertTrue(user.getLockoutEndTime().isAfter(LocalDateTime.now()));
    }

    @Test
    public void shouldNotIncrementFailedAttemptsWhenUserLocked() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setFailedLoginAttempts(1);
        user.setLockoutEndTime(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(authentication, new BadCredentialsException("Bad credentials"));

        authenticationListener.onApplicationEvent(event);

        assertEquals(1, user.getFailedLoginAttempts());
    }

    @Test
    public void shouldResetFailedAttemptsAndUnlockUserOnSuccessfulLogin() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setFailedLoginAttempts(2);
        user.setLockoutEndTime(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        authenticationListener.onApplicationEvent(event);

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockoutEndTime());
    }

    @Test
    public void shouldNotResetAttemptsWhenUserLockedOnSuccessfulLogin() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setFailedLoginAttempts(3);
        user.setLockoutEndTime(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        authenticationListener.onApplicationEvent(event);

        assertEquals(3, user.getFailedLoginAttempts());
        assertNotNull(user.getLockoutEndTime());
    }

    @Test
    public void shouldThrowExceptionWhenUserNotFoundOnFailedLogin() {
        String username = "nonExistentUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(authentication, new BadCredentialsException("Bad credentials"));

        assertThrows(UsernameNotFoundException.class, () -> authenticationListener.onApplicationEvent(event));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotFoundOnSuccessfulLogin() {
        String username = "nonExistentUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        assertThrows(UsernameNotFoundException.class, () -> authenticationListener.onApplicationEvent(event));
    }

    @Test
    public void shouldUnlockUserIfLockExpiredOnFailedLogin() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(LocalDateTime.now().minusMinutes(5));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(authentication, new BadCredentialsException("Bad credentials"));

        authenticationListener.onApplicationEvent(event);

        assertNull(user.getLockoutEndTime());
        assertEquals(1, user.getFailedLoginAttempts());
    }

    @Test
    public void shouldUnlockUserIfLockExpiredOnSuccessfulLogin() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setFailedLoginAttempts(3);
        user.setLockoutEndTime(LocalDateTime.now().minusMinutes(5));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        authenticationListener.onApplicationEvent(event);

        assertNull(user.getLockoutEndTime());
        assertEquals(0, user.getFailedLoginAttempts());
    }
}
