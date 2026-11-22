package com.telyvo.platform.research;

import java.time.Instant;
import java.util.List;

public record ResearchResponse(String query, String answer, List<ResearchSource> sources, Instant retrievedAt) {}