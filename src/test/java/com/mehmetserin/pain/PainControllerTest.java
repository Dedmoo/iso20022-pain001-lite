package com.mehmetserin.pain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void build_endpoint() throws Exception {
        String body = """
                {
                  "executionDate": "2026-05-10",
                  "transfers": [{
                    "endToEndId": "E2E-9",
                    "amount": 100.00,
                    "currency": "EUR",
                    "debtor": {"name": "Alice", "iban": "DE89370400440532013000", "bic": "COBADEFFXXX"},
                    "creditor": {"name": "Bob", "iban": "GB82WEST12345698765432", "bic": "WESTGB22XXX"},
                    "remittance": "Rent"
                  }]
                }
                """;
        mockMvc.perform(post("/api/pain001/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").exists())
                .andExpect(jsonPath("$.xml").exists());
    }
}
