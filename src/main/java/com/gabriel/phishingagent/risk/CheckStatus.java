package com.gabriel.phishingagent.risk;

/*
 Result of a required check.
 NOT_APPLICABLE only when there was nothing to query (for example,
 RDAP for a host that is an IP address).
 A check that could not do its job, or was skipped for lack of time, is FAILED.
 */
public enum CheckStatus {
    SUCCEEDED,
    FAILED,
    NOT_APPLICABLE
}