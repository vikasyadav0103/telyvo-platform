package com.telyvo.platform.research;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;
import static org.hamcrest.Matchers.equalTo;

class ResearchServiceTest {
    @Test
    void returnsProviderAnswerAndOnlyHttpSources() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.you.com/v1/research"))
                .andExpect(method(POST))
                .andExpect(header("X-API-Key", equalTo("test-key")))
                .andRespond(withSuccess("""
                        {"output":{"content":"Spring Boot and Go have different trade-offs [[1]].",
                          "sources":[
                            {"title":"Spring","url":"https://spring.io","snippets":["Official docs"]},
                            {"title":"Unsafe","url":"javascript:alert(1)","snippets":["ignore"]}
                          ]}}
                        """, MediaType.APPLICATION_JSON));

        ResearchService service = new ResearchService(builder,
                new ResearchProperties("test-key", "https://api.you.com/v1/research", 30, "lite"));

        ResearchResponse response = service.research("Spring Boot vs Go");

        assertThat(response.answer()).contains("different trade-offs");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().getFirst().url()).isEqualTo("https://spring.io");
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
