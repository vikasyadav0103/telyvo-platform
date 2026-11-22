package com.telyvo.platform.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.telyvo.platform.discovery.DiscoveryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableCaching
@EnableConfigurationProperties(DiscoveryProperties.class)
public class DiscoveryConfig {
    @Bean
    RestClient.Builder restClientBuilder() { return RestClient.builder(); }

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("discovery");
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(200).expireAfterWrite(Duration.ofMinutes(5)));
        return manager;
    }
}