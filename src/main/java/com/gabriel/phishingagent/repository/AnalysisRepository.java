package com.gabriel.phishingagent.repository;

import com.gabriel.phishingagent.entity.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    /** History: newest first. The id breaks ties when two analyses share the same timestamp. */
    Page<Analysis> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);
}