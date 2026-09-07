package com.studyshield.studyshield.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns a JSON 401 response when an unauthenticated request reaches a protected
 * resource. Without an entry point Spring Security falls back to 403 for anonymous
 * access, which clients cannot distinguish from a real authorization denial.
 *
 * <p>When a Bearer token was supplied but rejected by {@link JwtAuthFilter}, the filter
 * records the reason on the request ({@link JwtAuthFilter#AUTH_ERROR_ATTR}) and this
 * entry point echoes it so the client can distinguish "no credentials" from
 * "credentials rejected" (e.g. expired token).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object error = request.getAttribute(JwtAuthFilter.AUTH_ERROR_ATTR);
        String message = error != null
                ? error.toString()
                : "Authentication required";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 401);
        body.put("error", "UNAUTHORIZED");
        body.put("message", message);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}