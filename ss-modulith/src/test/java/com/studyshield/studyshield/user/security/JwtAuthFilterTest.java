package com.studyshield.studyshield.user.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthFilterTest {

    private static final String SECRET = "012345678901234567890123456789012345678901234567890123456789";

    private JwtProvider jwtProvider;
    private JwtAuthFilter filter;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, 60_000);
        filter = new JwtAuthFilter(jwtProvider);
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenAuthenticatesAndChains() throws Exception {
        String token = jwtProvider.generateToken(null, 42L, "parent@test.com", "PARENT");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNotNull();
        assertThat(context.getAuthentication().getPrincipal()).isEqualTo("42");
        assertThat(request.getAttribute(JwtAuthFilter.AUTH_ERROR_ATTR)).isNull();
    }

    @Test
    void rejectedTokenRecordsReasonAndLeavesAnonymous() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer not.actually.a.jwt");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(request.getAttribute(JwtAuthFilter.AUTH_ERROR_ATTR))
                .isEqualTo(JwtAuthFilter.AUTH_ERROR_REJECTED);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void missingHeaderDoesNotRecordReason() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(request.getAttribute(JwtAuthFilter.AUTH_ERROR_ATTR)).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}