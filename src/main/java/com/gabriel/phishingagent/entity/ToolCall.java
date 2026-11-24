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
@Table(name = "tool_call")
@Getter
@Setter
@NoArgsConstructor
public class ToolCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Column(name = "tool_name", nullable = false, length = 60)
    private String toolName;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoked_by", nullable = false, length = 10)
    private InvokedBy invokedBy;

    @Column(name = "input", columnDefinition = "text")
    private String input;

    @Column(name = "output", columnDefinition = "text")
    private String output;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "error", length = 500)
    private String error;

    @Column(name = "duration_ms")
    private Long durationMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}