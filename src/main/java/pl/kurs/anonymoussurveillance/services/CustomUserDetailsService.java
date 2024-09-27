package pl.kurs.anonymoussurveillance.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.kurs.anonymoussurveillance.models.User;
import pl.kurs.anonymoussurveillance.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("My lord, user with username: " + username + " does not exist."));

//        System.out.println("Loading user: " + username);
//        System.out.println("Roles: " + user.getRoles());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isAccountNonLocked(),
                true,
                true,
                true,
                user.getAuthorities()
        );
    }
}
