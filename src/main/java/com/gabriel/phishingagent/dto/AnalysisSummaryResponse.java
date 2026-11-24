package com.gabriel.phishingagent.dto;

import com.gabriel.phishingagent.entity.Analysis;
import com.gabriel.phishingagent.entity.AnalysisStatus;
import com.gabriel.phishingagent.entity.Coverage;
import com.gabriel.phishingagent.entity.RiskLevel;

import java.time.Instant;

public record AnalysisSummaryResponse(
        Long id,
        AnalysisStatus status,
        String subject,
        Integer riskScore,
        RiskLevel riskLevel,
        Coverage coverage,
        Instant createdAt
) {

    public static AnalysisSummaryResponse from(Analysis analysis) {
        return new AnalysisSummaryResponse(
                analysis.getId(),
                analysis.getStatus(),
                analysis.getSubject(),
                analysis.getRiskScore(),
                analysis.getRiskLevel(),
                analysis.getCoverage(),
                analysis.getCreatedAt()
        );
    }
}