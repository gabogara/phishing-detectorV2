package com.gabriel.phishingagent.risk;

import com.gabriel.phishingagent.entity.Coverage;
import com.gabriel.phishingagent.entity.RiskLevel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/*
 Fixed Java rules that turn findings into a score, a level and a coverage.
 The AI explains; this class decides. No network, no AI, no database: pure logic.
 The score is a heuristic rules score, not a probability.
 */
@Component
public class RiskPolicy {

    public static final String RULES_VERSION = "1.0";

    static final int AI_POINTS_CAP = 15;
    static final int MEDIUM_THRESHOLD = 40;
    static final int HIGH_THRESHOLD = 70;
    static final int MAX_SCORE = 100;

    public RiskAssessment assess(List<Finding> findings, Map<RequiredCheck, CheckStatus> checkStatuses) {
        Objects.requireNonNull(findings, "findings");
        Objects.requireNonNull(checkStatuses, "checkStatuses");

        // 1. Score. Each code counts once: five mismatched links are one signal, not five.
        //    The individual pieces of evidence stay in tool_call and in the findings JSON.
        Set<FindingCode> codes = findings.stream()
                .map(Finding::code)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(FindingCode.class)));

        int systemPoints = sumPoints(codes, FindingSource.SYSTEM);
        int aiPoints = Math.min(sumPoints(codes, FindingSource.AI), AI_POINTS_CAP);
        int rawScore = systemPoints + aiPoints;

        int floor = codes.stream().mapToInt(FindingCode::floor).max().orElse(0);
        boolean floorApplied = floor > rawScore;
        int score = Math.min(Math.max(rawScore, floor), MAX_SCORE);

        // 2. Coverage. Every required check is evaluated, even if the caller did not report it:
        //    a check that never ran is a failure, never a silent success.
        List<String> failedChecks = new ArrayList<>();
        List<String> notApplicableChecks = new ArrayList<>();
        boolean criticalCheckFailed = false;

        for (RequiredCheck check : RequiredCheck.values()) {
            CheckStatus status = checkStatuses.getOrDefault(check, CheckStatus.FAILED);
            switch (status) {
                case FAILED -> {
                    failedChecks.add(check.toolName());
                    if (check.critical()) {
                        criticalCheckFailed = true;
                    }
                }
                case NOT_APPLICABLE -> notApplicableChecks.add(check.toolName());
                case SUCCEEDED -> {
                    // nothing to record
                }
            }
        }

        // FULL = every required and applicable check worked, within the MVP scope (attachments are out of scope).
        // It does not mean the email is safe.
        Coverage coverage = failedChecks.isEmpty() ? Coverage.FULL : Coverage.PARTIAL;

        // 3. Level. "No signals" after a critical check failed may only mean "we could not look":
        //    that is never LOW. Strong evidence (MEDIUM or HIGH) still counts.
        RiskLevel level = levelFor(score);

        if (criticalCheckFailed && level == RiskLevel.LOW) {
            return new RiskAssessment(null, RiskLevel.INCONCLUSIVE, coverage,
                    systemPoints, aiPoints, floorApplied,
                    List.copyOf(failedChecks), List.copyOf(notApplicableChecks));
        }

        return new RiskAssessment(score, level, coverage,
                systemPoints, aiPoints, floorApplied,
                List.copyOf(failedChecks), List.copyOf(notApplicableChecks));
    }

    static RiskLevel levelFor(int score) {
        if (score >= HIGH_THRESHOLD) {
            return RiskLevel.HIGH;
        }
        if (score >= MEDIUM_THRESHOLD) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private static int sumPoints(Set<FindingCode> codes, FindingSource source) {
        return codes.stream()
                .filter(code -> code.source() == source)
                .mapToInt(FindingCode::points)
                .sum();
    }
}