package com.gabriel.phishingagent.entity;

/** Execution state of an analysis, independent of its risk. */
public enum AnalysisStatus {
    RUNNING,
    COMPLETED,
    FAILED
}