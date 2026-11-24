package com.gabriel.phishingagent.repository;

import com.gabriel.phishingagent.entity.Analysis;
import com.gabriel.phishingagent.entity.AnalysisStatus;
import com.gabriel.phishingagent.entity.Coverage;
import com.gabriel.phishingagent.entity.InvokedBy;
import com.gabriel.phishingagent.entity.RiskLevel;
import com.gabriel.phishingagent.entity.ToolCall;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AnalysisRepositoryTest {

    private static final String SHA256 = "a".repeat(64);

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ToolCallRepository toolCallRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesNewAnalysisAsRunning() {
        Analysis saved = analysisRepository.saveAndFlush(newAnalysis("Your account has been suspended"));
        entityManager.clear(); // force the next read to come from PostgreSQL, not from the cache

        Analysis found = analysisRepository.findById(saved.getId()).orElseThrow();

        assertThat(found).isNotSameAs(saved);
        assertThat(found.getStatus()).isEqualTo(AnalysisStatus.RUNNING);
        assertThat(found.getSha256()).isEqualTo(SHA256);
        assertThat(found.getSubject()).isEqualTo("Your account has been suspended");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getRiskLevel()).isNull();
        assertThat(found.getCompletedAt()).isNull();
    }

    @Test
    void completesAnalysisAndReadsItsTimelineInOrder() {
        // 1. The analysis starts as RUNNING, so tool calls can reference its id
        Analysis analysis = analysisRepository.saveAndFlush(newAnalysis("Invoice overdue"));

        // 2. Tool calls are saved while the analysis runs
        toolCallRepository.saveAndFlush(toolCall(analysis.getId(), "headerAnalysis", InvokedBy.SYSTEM, true, null));
        toolCallRepository.saveAndFlush(toolCall(analysis.getId(), "extractLinks", InvokedBy.SYSTEM, true, null));
        toolCallRepository.saveAndFlush(toolCall(analysis.getId(), "investigateDomain", InvokedBy.AGENT, false,
                "RDAP not supported for this TLD"));

        // 3. The analysis is completed at the end
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setRiskScore(85);
        analysis.setRiskLevel(RiskLevel.HIGH);
        analysis.setCoverage(Coverage.FULL);
        analysis.setCompletedAt(Instant.now());
        analysisRepository.saveAndFlush(analysis);

        // 4. Empty the cache, so everything below is read back from PostgreSQL
        entityManager.clear();

        List<ToolCall> timeline = toolCallRepository.findByAnalysisIdOrderByIdAsc(analysis.getId());

        assertThat(timeline)
                .extracting(ToolCall::getToolName)
                .containsExactly("headerAnalysis", "extractLinks", "investigateDomain");
        assertThat(timeline.get(2).getInvokedBy()).isEqualTo(InvokedBy.AGENT);
        assertThat(timeline.get(2).getSuccess()).isFalse();
        assertThat(timeline.get(2).getError()).isEqualTo("RDAP not supported for this TLD");

        Analysis found = analysisRepository.findById(analysis.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(found.getRiskScore()).isEqualTo(85);
        assertThat(found.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(found.getCoverage()).isEqualTo(Coverage.FULL);
        assertThat(found.getCompletedAt()).isNotNull();
    }

    @Test
    void listsHistoryNewestFirst() {
        Analysis older = analysisRepository.saveAndFlush(newAnalysis("Older email"));
        Analysis newer = analysisRepository.saveAndFlush(newAnalysis("Newer email"));
        entityManager.clear();

        Page<Analysis> page = analysisRepository.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(Analysis::getId)
                .containsSubsequence(newer.getId(), older.getId());
    }

    @Test
    void databaseRejectsHighLevelWithoutScore() {
        Analysis invalid = newAnalysis("Invalid result");
        invalid.setStatus(AnalysisStatus.COMPLETED);
        invalid.setRiskLevel(RiskLevel.HIGH);
        invalid.setRiskScore(null);
        invalid.setCoverage(Coverage.FULL);
        invalid.setCompletedAt(Instant.now().plusSeconds(1));

        assertThatThrownBy(() -> analysisRepository.saveAndFlush(invalid))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Analysis newAnalysis(String subject) {
        Analysis analysis = new Analysis();
        analysis.setSha256(SHA256);
        analysis.setSubject(subject);
        analysis.setRulesVersion("1.0");
        return analysis;
    }

    private static ToolCall toolCall(Long analysisId, String toolName, InvokedBy invokedBy,
                                     boolean success, String error) {
        ToolCall toolCall = new ToolCall();
        toolCall.setAnalysisId(analysisId);
        toolCall.setToolName(toolName);
        toolCall.setInvokedBy(invokedBy);
        toolCall.setSuccess(success);
        toolCall.setError(error);
        toolCall.setDurationMs(120L);
        return toolCall;
    }
}