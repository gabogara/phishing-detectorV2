package com.gabriel.phishingagent.service;

import com.gabriel.phishingagent.entity.InvokedBy;
import com.gabriel.phishingagent.entity.ToolCall;
import com.gabriel.phishingagent.exception.ErrorMessages;
import com.gabriel.phishingagent.repository.ToolCallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolCallLog {

    static final int MAX_INPUT_LENGTH = 2_000;
    static final int MAX_OUTPUT_LENGTH = 20_000;
    static final String TRUNCATION_MARKER = "...[truncated]";

    private final ToolCallRepository toolCallRepository;

    public ToolCallResult run(Long analysisId, String toolName, InvokedBy invokedBy,
                              String input, Supplier<String> tool) {
        long start = System.nanoTime();
        String output = null;
        String error = null;

        try {
            output = tool.get();
        } catch (Exception ex) {
            error = ErrorMessages.describe(ex);
        }

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        boolean success = error == null;

        ToolCall toolCall = new ToolCall();
        toolCall.setAnalysisId(analysisId);
        toolCall.setToolName(toolName);
        toolCall.setInvokedBy(invokedBy);
        toolCall.setInput(truncate(input, MAX_INPUT_LENGTH));
        toolCall.setOutput(truncate(output, MAX_OUTPUT_LENGTH));
        toolCall.setSuccess(success);
        toolCall.setError(error);
        toolCall.setDurationMs(durationMs);

        ToolCall saved = toolCallRepository.save(toolCall);

        // Only metadata is logged: inputs and outputs may contain email content
        log.info("Tool {} ({}) finished in {} ms, success={}", toolName, invokedBy, durationMs, success);

        return new ToolCallResult(saved.getId(), success, saved.getOutput(), error);
    }

    static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - TRUNCATION_MARKER.length()) + TRUNCATION_MARKER;
    }
}