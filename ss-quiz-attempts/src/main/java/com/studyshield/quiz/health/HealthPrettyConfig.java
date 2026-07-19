package com.studyshield.quiz.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class HealthPrettyConfig {

    @Bean
    public OncePerRequestFilter healthPrettyPrintFilter() {
        return new OncePerRequestFilter() {
            private final ObjectMapper prettyMapper = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                if ("/actuator/health".equals(request.getRequestURI())) {
                    HealthResponseWrapper wrappedResponse = new HealthResponseWrapper(response);
                    filterChain.doFilter(request, wrappedResponse);

                    byte[] originalBody = wrappedResponse.getBody();
                    if (originalBody != null && originalBody.length > 0) {
                        try {
                            Object json = prettyMapper.readValue(originalBody, Object.class);
                            byte[] prettyBody = prettyMapper.writeValueAsBytes(json);
                            response.getOutputStream().write(prettyBody);
                            response.getOutputStream().flush();
                        } catch (Exception e) {
                            response.getOutputStream().write(originalBody);
                            response.getOutputStream().flush();
                        }
                    }
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        };
    }
}
