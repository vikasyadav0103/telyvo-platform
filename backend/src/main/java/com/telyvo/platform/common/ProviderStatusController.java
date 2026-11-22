package com.telyvo.platform.common;

import com.telyvo.platform.collaboration.ChatProperties;
import com.telyvo.platform.discovery.DiscoveryProperties;
import com.telyvo.platform.research.ResearchProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/integrations")
public class ProviderStatusController {
    private final DiscoveryProperties discovery;
    private final ResearchProperties research;
    private final ChatProperties chat;

    public ProviderStatusController(DiscoveryProperties discovery, ResearchProperties research, ChatProperties chat) {
        this.discovery = discovery;
        this.research = research;
        this.chat = chat;
    }

    @GetMapping("/status")
    public List<ProviderStatus> status() {
        return List.of(
                new ProviderStatus("SerpApi", configured(discovery.serpApiKey()), "Live web discovery"),
                new ProviderStatus("You.com", configured(research.apiKey()), "Cited research synthesis"),
                new ProviderStatus("Nexconn", configured(chat.appKey()) && configured(chat.userToken())
                        && configured(chat.userId()) && configured(chat.groupId()), "Real-time group discussion")
        );
    }

    private boolean configured(String value) { return value != null && !value.isBlank(); }

    public record ProviderStatus(String provider, boolean configured, String capability) {}
}