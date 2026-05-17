package com.mehmetserin.pain.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class Pain001Builder {

    public record Party(String name, String iban, String bic) {}
    public record CreditTransfer(
            String endToEndId,
            BigDecimal amount,
            String currency,
            Party debtor,
            Party creditor,
            String remittance
    ) {}
    public record PainDocument(String messageId, LocalDate executionDate, List<CreditTransfer> transfers, String xml) {}

    public PainDocument build(LocalDate executionDate, List<CreditTransfer> transfers) {
        if (executionDate == null) {
            throw new IllegalArgumentException("Execution date is required.");
        }
        if (transfers == null || transfers.isEmpty()) {
            throw new IllegalArgumentException("At least one credit transfer is required.");
        }
        for (CreditTransfer t : transfers) {
            validateTransfer(t);
        }
        String messageId = "MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String created = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        BigDecimal ctrlSum = transfers.stream()
                .map(CreditTransfer::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">\n");
        xml.append("  <CstmrCdtTrfInitn>\n");
        xml.append("    <GrpHdr>\n");
        xml.append("      <MsgId>").append(escape(messageId)).append("</MsgId>\n");
        xml.append("      <CreDtTm>").append(escape(created)).append("</CreDtTm>\n");
        xml.append("      <NbOfTxs>").append(transfers.size()).append("</NbOfTxs>\n");
        xml.append("      <CtrlSum>").append(ctrlSum).append("</CtrlSum>\n");
        xml.append("      <InitgPty><Nm>").append(escape(transfers.get(0).debtor().name())).append("</Nm></InitgPty>\n");
        xml.append("    </GrpHdr>\n");

        int i = 1;
        for (CreditTransfer t : transfers) {
            String pmtInfId = "PMT-" + i++;
            xml.append("    <PmtInf>\n");
            xml.append("      <PmtInfId>").append(escape(pmtInfId)).append("</PmtInfId>\n");
            xml.append("      <PmtMtd>TRF</PmtMtd>\n");
            xml.append("      <ReqdExctnDt>").append(executionDate).append("</ReqdExctnDt>\n");
            xml.append("      <Dbtr><Nm>").append(escape(t.debtor().name())).append("</Nm></Dbtr>\n");
            xml.append("      <DbtrAcct><Id><IBAN>").append(escape(normalize(t.debtor().iban()))).append("</IBAN></Id></DbtrAcct>\n");
            if (t.debtor().bic() != null && !t.debtor().bic().isBlank()) {
                xml.append("      <DbtrAgt><FinInstnId><BIC>").append(escape(normalize(t.debtor().bic()))).append("</BIC></FinInstnId></DbtrAgt>\n");
            }
            xml.append("      <CdtTrfTxInf>\n");
            xml.append("        <PmtId><EndToEndId>").append(escape(t.endToEndId())).append("</EndToEndId></PmtId>\n");
            xml.append("        <Amt><InstdAmt Ccy=\"").append(escape(t.currency().toUpperCase(Locale.ROOT))).append("\">")
                    .append(t.amount().setScale(2, RoundingMode.HALF_UP)).append("</InstdAmt></Amt>\n");
            xml.append("        <CdtrAgt><FinInstnId><BIC>").append(escape(normalize(t.creditor().bic()))).append("</BIC></FinInstnId></CdtrAgt>\n");
            xml.append("        <Cdtr><Nm>").append(escape(t.creditor().name())).append("</Nm></Cdtr>\n");
            xml.append("        <CdtrAcct><Id><IBAN>").append(escape(normalize(t.creditor().iban()))).append("</IBAN></Id></CdtrAcct>\n");
            if (t.remittance() != null && !t.remittance().isBlank()) {
                xml.append("        <RmtInf><Ustrd>").append(escape(t.remittance())).append("</Ustrd></RmtInf>\n");
            }
            xml.append("      </CdtTrfTxInf>\n");
            xml.append("    </PmtInf>\n");
        }
        xml.append("  </CstmrCdtTrfInitn>\n");
        xml.append("</Document>\n");
        return new PainDocument(messageId, executionDate, transfers, xml.toString());
    }

    private void validateTransfer(CreditTransfer t) {
        if (t.amount() == null || t.amount().signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        if (t.currency() == null || t.currency().isBlank()) {
            throw new IllegalArgumentException("Currency is required.");
        }
        if (t.endToEndId() == null || t.endToEndId().isBlank()) {
            throw new IllegalArgumentException("EndToEndId is required.");
        }
        requireParty(t.debtor(), "Debtor");
        requireParty(t.creditor(), "Creditor");
    }

    private void requireParty(Party party, String label) {
        if (party == null || party.name() == null || party.name().isBlank()
                || party.iban() == null || party.iban().isBlank()
                || party.bic() == null || party.bic().isBlank()) {
            throw new IllegalArgumentException(label + " name, IBAN and BIC are required.");
        }
        if (!mod97Valid(normalize(party.iban()))) {
            throw new IllegalArgumentException(label + " IBAN failed mod-97 check.");
        }
    }

    private static boolean mod97Valid(String iban) {
        if (iban.length() < 15 || iban.length() > 34 || !iban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]+")) {
            return false;
        }
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else {
                numeric.append(c - 'A' + 10);
            }
        }
        return new java.math.BigInteger(numeric.toString()).mod(java.math.BigInteger.valueOf(97)).intValue() == 1;
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
