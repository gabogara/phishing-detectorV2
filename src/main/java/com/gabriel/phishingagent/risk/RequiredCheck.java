package com.gabriel.phishingagent.risk;

/*
 The required checks of the MVP. Java runs them on every analysis, with or without the agent.
 Critical checks decide whether LOW is allowed: without them,
  "no findings" may only mean "we could not look".
 */
public enum RequiredCheck {

    HEADER_ANALYSIS("headerAnalysis", true),
    EXTRACT_LINKS("extractLinks", true),
    BODY_TEXT("bodyText", false),
    RDAP_LOOKUP("rdapLookup", false),
    IP_INFO("ipInfo", false),
    REPUTATION("reputation", true);

    private final String toolName;
    private final boolean critical;

    RequiredCheck(String toolName, boolean critical) {
        this.toolName = toolName;
        this.critical = critical;
    }

    /* Same name as tool_call.tool_name, 
    so results can be matched with the timeline. */
    public String toolName() {
        return toolName;
    }

    public boolean critical() {
        return critical;
    }
}