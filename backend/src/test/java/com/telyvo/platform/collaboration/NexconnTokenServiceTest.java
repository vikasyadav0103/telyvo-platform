package com.telyvo.platform.collaboration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

class NexconnTokenServiceTest {
    @Test
    void requestsUserTokenWithSignedServerHeaders() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.sg-light-api.com/v4/auth/access-token/issue"))
                .andExpect(method(POST))
                .andExpect(header("App-Key", "demo-app-key"))
                .andExpect(header("Nonce", not(isEmptyOrNullString())))
                .andExpect(header("Timestamp", not(isEmptyOrNullString())))
                .andExpect(header("Signature", not(isEmptyOrNullString())))
                .andRespond(withSuccess("""
                        {"code":0,"result":{"userId":"demo-user","accessToken":"issued-demo-token"}}
                        """, MediaType.APPLICATION_JSON));

        NexconnTokenService service = new NexconnTokenService(
                new ChatProperties("demo-app-key", "demo-app-secret", "https://api.sg-light-api.com",
                        "demo-user", "team-room", "TELYVO Engineer", "peer-user", "TELYVO Teammate"), builder);

        assertThat(service.issueToken()).isEqualTo("issued-demo-token");
        server.verify();
    }
}
