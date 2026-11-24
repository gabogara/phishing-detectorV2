package com.gabriel.phishingagent.repository;

import com.gabriel.phishingagent.entity.ToolCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolCallRepository extends JpaRepository<ToolCall, Long> {

    /** Timeline of one analysis, in execution order. */
    List<ToolCall> findByAnalysisIdOrderByIdAsc(Long analysisId);
}