package com.telyvo.platform.research;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/research")
public class ResearchController {
    private final ResearchService service;
    public ResearchController(ResearchService service) { this.service = service; }

    @PostMapping
    public ResearchResponse research(@RequestBody ResearchRequest request) {
        return service.research(request.query().trim());
    }

    public record ResearchRequest(@NotBlank @Size(max = 4000) String query) {}
}