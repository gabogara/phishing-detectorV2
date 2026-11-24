package com.gabriel.phishingagent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "analysis")
@Getter
@Setter
@NoArgsConstructor
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisStatus status = AnalysisStatus.RUNNING;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "subject", length = 998)
    private String subject;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage", length = 10)
    private Coverage coverage;

    @Column(name = "findings", columnDefinition = "text")
    private String findings;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(name = "rules_version", nullable = false, length = 20)
    private String rulesVersion;

    @Column(name = "error", length = 500)
    private String error;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}