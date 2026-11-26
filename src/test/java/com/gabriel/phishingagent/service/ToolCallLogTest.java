package com.gabriel.phishingagent.service;

import com.gabriel.phishingagent.entity.InvokedBy;
import com.gabriel.phishingagent.entity.ToolCall;
import com.gabriel.phishingagent.repository.ToolCallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolCallLogTest {

    @Mock
    private ToolCallRepository toolCallRepository;

    @InjectMocks
    private ToolCallLog toolCallLog;

    @BeforeEach
    void saveAssignsAnId() {
        lenient().when(toolCallRepository.save(any(ToolCall.class))).thenAnswer(invocation -> {
            ToolCall toolCall = invocation.getArgument(0);
            toolCall.setId(7L);
            return toolCall;
        });
    }

    @Test
    void recordsSuccessfulTool() {
        ToolCallResult result = toolCallLog.run(1L, "rdapLookup", InvokedBy.SYSTEM,
                "example.com", () -> "{\"registered\":\"2020-01-01\"}");

        ToolCall saved = capturedToolCall();
        assertThat(saved.getAnalysisId()).isEqualTo(1L);
        assertThat(saved.getToolName()).isEqualTo("rdapLookup");
        assertThat(saved.getInvokedBy()).isEqualTo(InvokedBy.SYSTEM);
        assertThat(saved.getInput()).isEqualTo("example.com");
        assertThat(saved.getOutput()).isEqualTo("{\"registered\":\"2020-01-01\"}");
        assertThat(saved.getSuccess()).isTrue();
        assertThat(saved.getError()).isNull();
        assertThat(saved.getDurationMs()).isGreaterThanOrEqualTo(0L);

        assertThat(result.toolCallId()).isEqualTo(7L);
        assertThat(result.success()).isTrue();
    }

    @Test
    void recordsFailingToolWithoutThrowing() {
        ToolCallResult result = toolCallLog.run(1L, "ipInfo", InvokedBy.AGENT, "example.com", () -> {
            throw new IllegalStateException("Read timed out");
        });

        ToolCall saved = capturedToolCall();
        assertThat(saved.getSuccess()).isFalse();
        assertThat(saved.getError()).isEqualTo("IllegalStateException: Read timed out");
        assertThat(saved.getOutput()).isNull();

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isEqualTo("IllegalStateException: Read timed out");
    }

    @Test
    void truncatesLongOutputToTheLimit() {
        String longOutput = "x".repeat(ToolCallLog.MAX_OUTPUT_LENGTH + 500);

        ToolCallResult result = toolCallLog.run(1L, "bodyText", InvokedBy.SYSTEM, null, () -> longOutput);

        ToolCall saved = capturedToolCall();
        assertThat(saved.getOutput()).hasSize(ToolCallLog.MAX_OUTPUT_LENGTH);
        assertThat(saved.getOutput()).endsWith(ToolCallLog.TRUNCATION_MARKER);
        // The caller gets exactly what was stored, so AI quotes are checked against the same text
        assertThat(result.output()).isEqualTo(saved.getOutput());
    }

    @Test
    void keepsShortTextUntouched() {
        assertThat(ToolCallLog.truncate("short", 10)).isEqualTo("short");
        assertThat(ToolCallLog.truncate(null, 10)).isNull();
    }

    private ToolCall capturedToolCall() {
        ArgumentCaptor<ToolCall> captor = ArgumentCaptor.forClass(ToolCall.class);
        verify(toolCallRepository).save(captor.capture());
        return captor.getValue();
    }
}