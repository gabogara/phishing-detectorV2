package com.gabriel.phishingagent.dto;

import com.gabriel.phishingagent.entity.InvokedBy;
import com.gabriel.phishingagent.entity.ToolCall;

import java.time.Instant;

public record ToolCallResponse(
        Long id,
        String toolName,
        InvokedBy invokedBy,
        String input,
        String output,
        Boolean success,
        String error,
        Long durationMs,
        Instant createdAt
) {

    public static ToolCallResponse from(ToolCall toolCall) {
        return new ToolCallResponse(
                toolCall.getId(),
                toolCall.getToolName(),
                toolCall.getInvokedBy(),
                toolCall.getInput(),
                toolCall.getOutput(),
                toolCall.getSuccess(),
                toolCall.getError(),
                toolCall.getDurationMs(),
                toolCall.getCreatedAt()
        );
    }
}