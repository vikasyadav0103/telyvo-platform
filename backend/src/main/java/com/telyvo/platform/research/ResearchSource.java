package com.telyvo.platform.research;

import java.util.List;

public record ResearchSource(int sourceIndex, String title, String url, List<String> snippets) {}
