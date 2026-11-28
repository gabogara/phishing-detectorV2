package com.gabriel.phishingagent.risk;

import java.util.Objects;

/*
 One finding: what was found, where the evidence is (tool_call id) and why.
  AI findings also carry the exact quote that supports them.
 */
public record Finding(
        FindingCode code,
        Long toolCallId,
        String detail,
        String quote
) {

    public Finding {
        Objects.requireNonNull(code, "code");
    }

    public static Finding system(FindingCode code, Long toolCallId, String detail) {
        if (code.source() != FindingSource.SYSTEM) {
            throw new IllegalArgumentException(code + " is not a SYSTEM finding");
        }
        return new Finding(code, toolCallId, detail, null);
    }

    public static Finding ai(FindingCode code, Long toolCallId, String quote) {
        if (code.source() != FindingSource.AI) {
            throw new IllegalArgumentException(code + " is not an AI finding");
        }
        return new Finding(code, toolCallId, null, quote);
    }

    public FindingSource source() {
        return code.source();
    }
}