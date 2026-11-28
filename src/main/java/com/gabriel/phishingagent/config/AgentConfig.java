package com.gabriel.phishingagent.config;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AgentConfig {

    @Bean
    public ChatModel chatModel(
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model}") String modelName,
            @Value("${app.ai.max-tokens}") int maxTokens,
            @Value("${app.ai.timeout}") Duration timeout) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "app.ai.api-key is not set. Add ANTHROPIC_API_KEY to your .env file.");
        }

        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .timeout(timeout)
                .maxRetries(1)
                .logRequests(false)
                .logResponses(false)
                .build();
    }
}