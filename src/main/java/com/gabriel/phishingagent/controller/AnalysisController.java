package com.gabriel.phishingagent.controller;

import com.gabriel.phishingagent.dto.AnalysisDetailResponse;
import com.gabriel.phishingagent.dto.AnalysisSummaryResponse;
import com.gabriel.phishingagent.dto.PageResponse;
import com.gabriel.phishingagent.service.AnalysisQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisQueryService analysisQueryService;

    @GetMapping
    public PageResponse<AnalysisSummaryResponse> listHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return analysisQueryService.listHistory(page, size);
    }

    @GetMapping("/{id}")
    public AnalysisDetailResponse getDetail(@PathVariable Long id) {
        return analysisQueryService.getDetail(id);
    }
}