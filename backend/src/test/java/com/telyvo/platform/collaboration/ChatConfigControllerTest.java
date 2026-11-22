package com.telyvo.platform.collaboration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ChatConfigControllerTest {
    private ChatProperties properties(boolean demoMode) {
        return new ChatProperties("app-key", "app-secret", "https://api.sg-light-api.com",
                "primary-user", "team-room", "Primary", "peer-user", "Peer", demoMode);
    }

    @Test
    void issuesTokensOnlyForConfiguredDemoParticipants() {
        NexconnTokenService tokenService = mock(NexconnTokenService.class);
        when(tokenService.issueToken("primary-user", "Primary")).thenReturn("signed-token");
        ChatConfigController controller = new ChatConfigController(properties(true), tokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.10");

        ChatConfigController.ChatConfig config = controller.config("primary", request);

        assertThat(config.appKey()).isEqualTo("app-key");
        assertThat(config.token()).isEqualTo("signed-token");
        assertThat(config.userId()).isEqualTo("primary-user");
        assertThat(config.groupId()).isEqualTo("team-room");
        verify(tokenService).issueToken("primary-user", "Primary");
    }

    @Test
    void blocksTokenIssuanceWhenDemoModeIsDisabled() {
        NexconnTokenService tokenService = mock(NexconnTokenService.class);
        ChatConfigController controller = new ChatConfigController(properties(false), tokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.11");

        assertThatThrownBy(() -> controller.config("primary", request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(403);
        verifyNoInteractions(tokenService);
    }

    @Test
    void rateLimitsTokenIssuancePerClientIp() {
        NexconnTokenService tokenService = mock(NexconnTokenService.class);
        when(tokenService.issueToken("primary-user", "Primary")).thenReturn("signed-token");
        ChatConfigController controller = new ChatConfigController(properties(true), tokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.12");

        for (int i = 0; i < 10; i++) controller.config("primary", request);

        assertThatThrownBy(() -> controller.config("primary", request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(429);
        verify(tokenService, times(10)).issueToken("primary-user", "Primary");
    }
}
