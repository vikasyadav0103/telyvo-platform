package com.telyvo.platform.research;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.http.HttpMethod.POST;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

class ResearchServiceTest {
    @Test
    void returnsProviderAnswerAndOnlyHttpSources() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.you.com/v1/research"))
                .andExpect(method(POST))
                .andExpect(header("X-API-Key", equalTo("test-key")))
                .andExpect(content().string(containsString("evidence-based engineering decision brief")))
                .andRespond(withSuccess("""
                        {"output":{"content":"Spring Boot and Go have different trade-offs [[1]].",
                          "sources":[
                            {"title":"Unsafe","url":"javascript:alert(1)","snippets":["ignore"]},
                            {"title":"Spring","url":"https://spring.io","snippets":["Official docs"]}
                          ]}}
                        """, MediaType.APPLICATION_JSON));

        ResearchService service = new ResearchService(builder,
                new ResearchProperties("test-key", "https://api.you.com/v1/research", 30, "lite"));

        ResearchResponse response = service.research("Spring Boot vs Go");

        assertThat(response.answer()).contains("different trade-offs");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().getFirst().url()).isEqualTo("https://spring.io");
        assertThat(response.sources().getFirst().sourceIndex()).isEqualTo(2);
        server.verify();
    }

    @Test
    void mapsProviderRateLimitTo429() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.you.com/v1/research"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        ResearchService service = new ResearchService(builder,
                new ResearchProperties("test-key", "https://api.you.com/v1/research", 30, "lite"));

        assertThatThrownBy(() -> service.research("compare databases"))
                .isInstanceOf(ResearchException.class)
                .hasMessageContaining("rate limit");
        server.verify();
    }

    @Test
    void rejectsMissingApiKeyWithoutCallingProvider() {
        ResearchService service = new ResearchService(RestClient.builder(),
                new ResearchProperties("", null, 30, "lite"));

        assertThatThrownBy(() -> service.research("compare databases"))
                .isInstanceOf(ResearchException.class)
                .hasMessageContaining("Add YOU_API_KEY");
    }
}
