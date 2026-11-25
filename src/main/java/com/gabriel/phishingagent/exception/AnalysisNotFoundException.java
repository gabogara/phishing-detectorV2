package com.gabriel.phishingagent.exception;

public class AnalysisNotFoundException extends RuntimeException {

    public AnalysisNotFoundException(Long id) {
        super("Analysis " + id + " not found");
    }
}