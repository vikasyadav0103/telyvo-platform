package com.telyvo.platform.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class ProviderRateLimitInterceptorTest {
    @Test
    void limitsDiscoveryToTwentyRequestsPerMinutePerIp() {
        ProviderRateLimitInterceptor interceptor = new ProviderRateLimitInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/discovery");
        request.setRemoteAddr("192.0.2.30");
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 20; i++) {
            assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        }

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(429);
    }

    @Test
    void appliesLowerLimitToExpensiveResearchRequests() {
        ProviderRateLimitInterceptor interceptor = new ProviderRateLimitInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/research");
        request.setRemoteAddr("192.0.2.31");
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 3; i++) {
            assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        }

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(429);
    }
}
