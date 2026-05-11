package com.mehmetserin.pain.web;

import com.mehmetserin.pain.service.Pain001Builder;
import com.mehmetserin.pain.service.Pain001Builder.CreditTransfer;
import com.mehmetserin.pain.service.Pain001Builder.PainDocument;
import com.mehmetserin.pain.service.Pain001Builder.Party;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pain001")
public class PainController {

    public record BuildRequest(
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate executionDate,
            @NotEmpty List<CreditTransfer> transfers
    ) {}

    private final Pain001Builder builder;

    public PainController(Pain001Builder builder) {
        this.builder = builder;
    }

    @PostMapping(value = "/build", produces = MediaType.APPLICATION_JSON_VALUE)
    public PainDocument build(@Valid @RequestBody BuildRequest request) {
        return builder.build(request.executionDate(), request.transfers());
    }

    @PostMapping(value = "/build.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String buildXml(@Valid @RequestBody BuildRequest request) {
        return builder.build(request.executionDate(), request.transfers()).xml();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "healthy", "service", "iso20022-pain001-lite");
    }
}
