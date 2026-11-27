package com.gabriel.phishingagent.controller;

import com.gabriel.phishingagent.config.ApiKeyFilter;
import com.gabriel.phishingagent.dto.AnalysisDetailResponse;
import com.gabriel.phishingagent.dto.AnalysisSummaryResponse;
import com.gabriel.phishingagent.dto.PageResponse;
import com.gabriel.phishingagent.dto.ToolCallResponse;
import com.gabriel.phishingagent.entity.AnalysisStatus;
import com.gabriel.phishingagent.entity.Coverage;
import com.gabriel.phishingagent.entity.InvokedBy;
import com.gabriel.phishingagent.entity.RiskLevel;
import com.gabriel.phishingagent.exception.AnalysisNotFoundException;
import com.gabriel.phishingagent.service.AnalysisQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@TestPropertySource(properties = "app.security.api-key=" + AnalysisControllerTest.API_KEY)
class AnalysisControllerTest {

    static final String API_KEY = "test-key";
    private static final Instant CREATED_AT = Instant.parse("2026-11-23T21:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisQueryService analysisQueryService;

    @Test
    void listsHistory() throws Exception {
        AnalysisSummaryResponse item = new AnalysisSummaryResponse(
                1L, AnalysisStatus.COMPLETED, "Invoice overdue", 45, RiskLevel.MEDIUM, Coverage.PARTIAL, CREATED_AT);
        when(analysisQueryService.listHistory(0, 20))
                .thenReturn(new PageResponse<>(List.of(item), 0, 20, 1, 1));

        mockMvc.perform(authorizedGet("/api/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].subject").value("Invoice overdue"))
                .andExpect(jsonPath("$.content[0].riskLevel").value("MEDIUM"))
                .andExpect(jsonPath("$.content[0].coverage").value("PARTIAL"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void returnsDetailWithFindingsAsJsonObjectAndTimeline() throws Exception {
        ToolCallResponse toolCall = new ToolCallResponse(
                10L, "headerAnalysis", InvokedBy.SYSTEM, null, "{\"spf\":\"fail\"}", true, null, 12L, CREATED_AT);
        AnalysisDetailResponse detail = new AnalysisDetailResponse(
                1L, AnalysisStatus.COMPLETED, "a".repeat(64), "Invoice overdue", 45, RiskLevel.MEDIUM,
                Coverage.PARTIAL, "{\"valid\":[{\"code\":\"SPF_FAIL\",\"points\":15}]}",
                "The email fails SPF.", null, "1.0", null, CREATED_AT, CREATED_AT, List.of(toolCall));
        when(analysisQueryService.getDetail(1L)).thenReturn(detail);

        mockMvc.perform(authorizedGet("/api/analyses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.findings.valid[0].code").value("SPF_FAIL"))
                .andExpect(jsonPath("$.findings.valid[0].points").value(15))
                .andExpect(jsonPath("$.toolCalls[0].toolName").value("headerAnalysis"))
                .andExpect(jsonPath("$.toolCalls[0].invokedBy").value("SYSTEM"));
    }

    @Test
    void unknownIdReturns404ProblemDetail() throws Exception {
        when(analysisQueryService.getDetail(99L)).thenThrow(new AnalysisNotFoundException(99L));

        mockMvc.perform(authorizedGet("/api/analyses/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Analysis not found"))
                .andExpect(jsonPath("$.detail").value("Analysis 99 not found"));
    }

    @Test
    void nonNumericIdReturns400ProblemDetail() throws Exception {
        mockMvc.perform(authorizedGet("/api/analyses/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unexpectedErrorReturns500WithoutInternalDetails() throws Exception {
        when(analysisQueryService.getDetail(5L)).thenThrow(new IllegalStateException("secret internal detail"));

        mockMvc.perform(authorizedGet("/api/analyses/5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }

    @Test
    void requestWithoutApiKeyIsRejectedBeforeTheController() throws Exception {
        mockMvc.perform(get("/api/analyses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Missing or invalid API key"));
    }

    private static MockHttpServletRequestBuilder authorizedGet(String url) {
        return get(url).header(ApiKeyFilter.HEADER_NAME, API_KEY);
    }
}