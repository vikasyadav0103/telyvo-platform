package com.telyvo.platform.discovery;

import java.time.Instant;
import java.util.List;

public record DiscoveryResponse(String query, List<DiscoveryResult> results, Instant retrievedAt) {}