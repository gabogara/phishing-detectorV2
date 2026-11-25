package com.gabriel.phishingagent.service;

import com.gabriel.phishingagent.dto.AnalysisDetailResponse;
import com.gabriel.phishingagent.dto.AnalysisSummaryResponse;
import com.gabriel.phishingagent.dto.PageResponse;
import com.gabriel.phishingagent.dto.ToolCallResponse;
import com.gabriel.phishingagent.entity.Analysis;
import com.gabriel.phishingagent.exception.AnalysisNotFoundException;
import com.gabriel.phishingagent.repository.AnalysisRepository;
import com.gabriel.phishingagent.repository.ToolCallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisQueryService {

    static final int MAX_PAGE_SIZE = 100;

    private final AnalysisRepository analysisRepository;
    private final ToolCallRepository toolCallRepository;

    public PageResponse<AnalysisSummaryResponse> listHistory(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        return PageResponse.from(
                analysisRepository
                        .findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(safePage, safeSize))
                        .map(AnalysisSummaryResponse::from)
        );
    }

    public AnalysisDetailResponse getDetail(Long id) {
        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new AnalysisNotFoundException(id));

        List<ToolCallResponse> toolCalls = toolCallRepository.findByAnalysisIdOrderByIdAsc(id)
                .stream()
                .map(ToolCallResponse::from)
                .toList();

        return AnalysisDetailResponse.from(analysis, toolCalls);
    }
}