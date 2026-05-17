package com.mehmetserin.pain;

import com.mehmetserin.pain.service.Pain001Builder;
import com.mehmetserin.pain.service.Pain001Builder.CreditTransfer;
import com.mehmetserin.pain.service.Pain001Builder.Party;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Pain001BuilderTest {

    private final Pain001Builder builder = new Pain001Builder();

    @Test
    void buildsXmlWithIbanAndAmount() {
        var doc = builder.build(LocalDate.of(2026, 5, 10), List.of(
                new CreditTransfer(
                        "E2E-1",
                        new BigDecimal("1250.50"),
                        "EUR",
                        new Party("Alice GmbH", "DE89370400440532013000", "COBADEFFXXX"),
                        new Party("Bob SA", "GB82WEST12345698765432", "WESTGB22XXX"),
                        "Invoice 42")
        ));
        assertTrue(doc.xml().contains("pain.001.001.03"));
        assertTrue(doc.xml().contains("DE89370400440532013000"));
        assertTrue(doc.xml().contains("1250.50"));
        assertTrue(doc.xml().contains("E2E-1"));
    }
}
