package com.telyvo.platform.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

class DiscoveryServiceTest {

    @Test
    void mapsProviderResultsToSafeResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("engine=google")))
                .andRespond(withSuccess("""
                        {"organic_results":[
                          {"title":"Spring Boot","link":"https://spring.io/projects/spring-boot","snippet":"Official project","source":"Spring"},
                          {"title":"Unsafe link","link":"javascript:alert(1)","snippet":"Ignore"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        DiscoveryService service = new DiscoveryService(builder,
                new DiscoveryProperties("test-key", "https://serpapi.com/search.json", 5, 8));

        DiscoveryResponse response = service.search("Spring Boot");

        assertThat(response.results()).hasSize(1);
        assertThat(response.results().getFirst().title()).isEqualTo("Spring Boot");
        assertThat(response.results().getFirst().url()).startsWith("https://");
        server.verify();
    }

    @Test
    void failsClearlyWhenApiKeyIsMissing() {
        DiscoveryService service = new DiscoveryService(RestClient.builder(),
                new DiscoveryProperties("", null, 5, 8));

        assertThatThrownBy(() -> service.search("Spring Boot"))
                .isInstanceOf(DiscoveryException.class)
                .hasMessageContaining("Add SERPAPI_KEY");
    }
}
