package pl.kurs.anonymoussurveillance.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class FailedLoginAttemptFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private FailedLoginAttemptFilter failedLoginAttemptFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldSetUsernameAttribute_WhenValidBasicAuthHeaderProvided() throws ServletException, IOException {
        String username = "testUser";
        String password = "testPass";
        String credentials = username + ":" + password;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        when(request.getHeader("Authorization")).thenReturn("Basic " + base64Credentials);

        Map<String, Object> attributeMap = new HashMap<>();
        doAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            Object value = invocation.getArgument(1);
            attributeMap.put(key, value);
            return null;
        }).when(request).setAttribute(anyString(), any());

        when(request.getAttribute(anyString())).thenAnswer(invocation -> attributeMap.get(invocation.getArgument(0, String.class)));

        failedLoginAttemptFilter.doFilterInternal(request, response, filterChain);

        assertEquals(username, attributeMap.get("lastAttemptedUsername"));
    }

    @Test
    public void shouldNotSetUsernameAttribute_WhenNoAuthHeaderProvided() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        failedLoginAttemptFilter.doFilterInternal(request, response, filterChain);

        assertNull(request.getAttribute("lastAttemptedUsername"));

    }

    @Test
    public void shouldNotSetUsernameAttribute_WhenNonBasicAuthHeaderProvided() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer someToken");

        failedLoginAttemptFilter.doFilterInternal(request, response, filterChain);

        assertNull(request.getAttribute("lastAttemptedUsername"));
        assertEquals("Bearer someToken", request.getHeader("Authorization"));
    }
}
