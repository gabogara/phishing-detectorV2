package com.gabriel.phishingagent.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.gabriel.phishingagent.entity.Analysis;
import com.gabriel.phishingagent.entity.AnalysisStatus;
import com.gabriel.phishingagent.entity.Coverage;
import com.gabriel.phishingagent.entity.RiskLevel;

import java.time.Instant;
import java.util.List;

public record AnalysisDetailResponse(
        Long id,
        AnalysisStatus status,
        String sha256,
        String subject,
        Integer riskScore,
        RiskLevel riskLevel,
        Coverage coverage,
        @JsonRawValue String findings,
        String summary,
        String aiModel,
        String rulesVersion,
        String error,
        Instant createdAt,
        Instant completedAt,
        List<ToolCallResponse> toolCalls
) {

    public static AnalysisDetailResponse from(Analysis analysis, List<ToolCallResponse> toolCalls) {
        return new AnalysisDetailResponse(
                analysis.getId(),
                analysis.getStatus(),
                analysis.getSha256(),
                analysis.getSubject(),
                analysis.getRiskScore(),
                analysis.getRiskLevel(),
                analysis.getCoverage(),
                analysis.getFindings(),
                analysis.getSummary(),
                analysis.getAiModel(),
                analysis.getRulesVersion(),
                analysis.getError(),
                analysis.getCreatedAt(),
                analysis.getCompletedAt(),
                toolCalls
        );
    }
}