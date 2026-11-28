package com.gabriel.phishingagent.risk;

import com.gabriel.phishingagent.entity.Coverage;
import com.gabriel.phishingagent.entity.RiskLevel;

import java.util.List;

/*
 The decision of RiskPolicy. riskScore is null when the level is INCONCLUSIVE.
 The breakdown (system points, AI points, floor) explains where the score came from.
 failedChecks lists required checks that failed or never ran; notApplicableChecks, those with nothing to query.
 */
public record RiskAssessment(
        Integer riskScore,
        RiskLevel riskLevel,
        Coverage coverage,
        int systemPoints,
        int aiPoints,
        boolean floorApplied,
        List<String> failedChecks,
        List<String> notApplicableChecks
) {
}