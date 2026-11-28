package com.gabriel.phishingagent.config;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
@SpringBootTest
class ClaudeConnectionTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    void claudeAnswers() {
        String answer = chatModel.chat("Reply with exactly one word: pong");

        assertThat(answer).containsIgnoringCase("pong");
    }
}