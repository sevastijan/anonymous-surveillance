package pl.kurs.anonymoussurveillance.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pl.kurs.anonymoussurveillance.models.User;
import pl.kurs.anonymoussurveillance.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AuthenticationListener implements ApplicationListener<ApplicationEvent> {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_TIME_IN_MINUTES = 10;

    private final UserRepository userRepository;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(event instanceof AuthenticationFailureBadCredentialsEvent) {
            handleFailedLogin((AuthenticationFailureBadCredentialsEvent) event);
        } else if (event instanceof AuthenticationSuccessEvent) {
            handleSuccessfulLogin((AuthenticationSuccessEvent) event);
        }
    }

    private void handleFailedLogin(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("My lord, user with username: " + username + " does not exist."));

        if(isAccountLocked(user)) return;

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if(user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
            user.setLockoutEndTime(LocalDateTime.now().plusMinutes(LOCK_TIME_IN_MINUTES));
            user.setFailedLoginAttempts(0);
        }

        userRepository.save(user);
    }

    private void handleSuccessfulLogin(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("My lord, user with username: " + username + " does not exist."));

        if(isAccountLocked(user)) return;

        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(null);

        userRepository.save(user);
    }

    private boolean isAccountLocked(User user) {
        LocalDateTime lockEndTime = user.getLockoutEndTime();
        if (lockEndTime != null) {
            if (LocalDateTime.now().isBefore(lockEndTime)) {
                return true;
            } else {
                user.setLockoutEndTime(null);
                userRepository.save(user);
            }
        }
        return false;
    }
}
