package com.gabriel.phishingagent.service;

public record ToolCallResult(
        Long toolCallId,
        boolean success,
        String output,
        String error
) {
}