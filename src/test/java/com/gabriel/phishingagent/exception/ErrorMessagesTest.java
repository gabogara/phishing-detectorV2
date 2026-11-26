package com.gabriel.phishingagent.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMessagesTest {

    @Test
    void combinesTypeAndMessage() {
        assertThat(ErrorMessages.describe(new IllegalStateException("Read timed out")))
                .isEqualTo("IllegalStateException: Read timed out");
    }

    @Test
    void usesTheTypeWhenThereIsNoMessage() {
        assertThat(ErrorMessages.describe(new NullPointerException()))
                .isEqualTo("NullPointerException");
        assertThat(ErrorMessages.describe(new IllegalStateException("   ")))
                .isEqualTo("IllegalStateException");
    }

    @Test
    void putsMultilineMessagesOnOneLine() {
        assertThat(ErrorMessages.describe(new IllegalStateException("line one\nline two")))
                .isEqualTo("IllegalStateException: line one line two");
    }

    @Test
    void neverExceedsTheColumnSize() {
        String result = ErrorMessages.describe(new IllegalStateException("x".repeat(1_000)));

        assertThat(result).hasSize(ErrorMessages.MAX_LENGTH);
        assertThat(result).endsWith("...");
    }

    @Test
    void neverReturnsBlank() {
        assertThat(ErrorMessages.describe(null)).isEqualTo("Unknown error");
    }
}