package com.gabriel.phishingagent.risk;

/*
    The closed list of findings and their fixed points (rules version 1.0).
  SYSTEM codes come from Java checks; AI codes are the only ones the AI may propose.
  A floor guarantees a minimum score, whatever the other findings say.
 */
public enum FindingCode {

    // Email headers (headerAnalysis). Headers are declared data: moderate weight.
    DMARC_FAIL(FindingSource.SYSTEM, 15),
    SPF_FAIL(FindingSource.SYSTEM, 10),
    DKIM_FAIL(FindingSource.SYSTEM, 10),
    REPLY_TO_MISMATCH(FindingSource.SYSTEM, 10),
    RETURN_PATH_MISMATCH(FindingSource.SYSTEM, 5),
    DISPLAY_NAME_SPOOFING(FindingSource.SYSTEM, 15),

    // Links (extractLinks)
    LINK_TEXT_MISMATCH(FindingSource.SYSTEM, 20),
    URL_SHORTENER(FindingSource.SYSTEM, 10),
    IP_AS_HOST(FindingSource.SYSTEM, 15),
    PUNYCODE_DOMAIN(FindingSource.SYSTEM, 15),

    // Domains (rdapLookup, reputation)
    YOUNG_DOMAIN(FindingSource.SYSTEM, 20),
    MALICIOUS_REPUTATION(FindingSource.SYSTEM, 0, 70),

    // AI interpretation of the content: capped in total by RiskPolicy
    BRAND_IMPERSONATION(FindingSource.AI, 10),
    CREDENTIAL_REQUEST(FindingSource.AI, 10),
    PAYMENT_REQUEST(FindingSource.AI, 10);

    private final FindingSource source;
    private final int points;
    private final int floor;

    FindingCode(FindingSource source, int points) {
        this(source, points, 0);
    }

    FindingCode(FindingSource source, int points, int floor) {
        this.source = source;
        this.points = points;
        this.floor = floor;
    }

    public FindingSource source() {
        return source;
    }

    public int points() {
        return points;
    }

    public int floor() {
        return floor;
    }
}