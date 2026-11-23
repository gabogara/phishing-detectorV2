-- =====================================================================
-- Phishing Detector: initial schema (MVP)
-- PostgreSQL 16
--
-- Every analysis starts from an .eml file (POST /api/analyses, multipart, field "file").
--
-- Enums are stored as VARCHAR + CHECK, not as CREATE TYPE ... AS ENUM.
-- In Java they are regular enums mapped with @Enumerated(EnumType.STRING).
--   AnalysisStatus : RUNNING | COMPLETED | FAILED
--   RiskLevel      : LOW | MEDIUM | HIGH | INCONCLUSIVE
--   Coverage       : FULL | PARTIAL
--   InvokedBy      : SYSTEM | AGENT
-- =====================================================================

-- ---------------------------------------------------------------------
-- analysis: one row per analyzed .eml file
-- ---------------------------------------------------------------------
CREATE TABLE analysis (
    id            BIGSERIAL     PRIMARY KEY,
    status        VARCHAR(20)   NOT NULL DEFAULT 'RUNNING',
    sha256        VARCHAR(64)   NOT NULL,
    subject       VARCHAR(998),
    risk_score    INT,
    risk_level    VARCHAR(20),
    coverage      VARCHAR(10),
    findings      TEXT,
    summary       TEXT,
    ai_model      VARCHAR(100),
    rules_version VARCHAR(20)   NOT NULL,
    error         VARCHAR(500),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    completed_at  TIMESTAMPTZ,

    -- Enums
    CONSTRAINT chk_analysis_status   CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_analysis_level    CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'INCONCLUSIVE')),
    CONSTRAINT chk_analysis_coverage CHECK (coverage IN ('FULL', 'PARTIAL')),

    -- Data format
    CONSTRAINT chk_analysis_sha256_hex CHECK (sha256 ~ '^[0-9a-f]{64}$'),

    -- Consistency between status and result
    CONSTRAINT chk_analysis_running_empty CHECK (status <> 'RUNNING'
        OR (risk_level IS NULL AND coverage IS NULL AND completed_at IS NULL)),
    CONSTRAINT chk_analysis_completed_result CHECK (status <> 'COMPLETED'
        OR (risk_level IS NOT NULL AND coverage IS NOT NULL AND completed_at IS NOT NULL)),
    CONSTRAINT chk_analysis_failed_no_result CHECK (status <> 'FAILED'
        OR (risk_level IS NULL AND coverage IS NULL AND completed_at IS NOT NULL)),
    -- The failure reason exists if and only if the analysis failed
    CONSTRAINT chk_analysis_error CHECK (
        (status = 'FAILED' AND error IS NOT NULL)
        OR
        (status <> 'FAILED' AND error IS NULL)
    ),
    CONSTRAINT chk_analysis_dates CHECK (completed_at IS NULL OR completed_at >= created_at),

    -- Consistency between score and level.
    -- CASE is used because PostgreSQL accepts a CHECK whose result is NULL:
    -- with a chain of ORs, a NULL risk_level with risk_score 85 evaluated to NULL and passed.
    -- Every branch of this CASE always returns TRUE or FALSE, never NULL.
    CONSTRAINT chk_analysis_score_matches_level CHECK (
        CASE
            WHEN risk_level IS NULL           THEN risk_score IS NULL
            WHEN risk_level = 'INCONCLUSIVE'  THEN risk_score IS NULL
            WHEN risk_level = 'LOW'           THEN risk_score IS NOT NULL AND risk_score BETWEEN 0  AND 39
            WHEN risk_level = 'MEDIUM'        THEN risk_score IS NOT NULL AND risk_score BETWEEN 40 AND 69
            WHEN risk_level = 'HIGH'          THEN risk_score IS NOT NULL AND risk_score BETWEEN 70 AND 100
            ELSE FALSE
        END
    )
);

-- Supports the history endpoint (newest first)
CREATE INDEX idx_analysis_created ON analysis (created_at DESC);

COMMENT ON TABLE  analysis               IS 'One analysis of an .eml file and its result';
COMMENT ON COLUMN analysis.status        IS 'AnalysisStatus: RUNNING | COMPLETED | FAILED. Execution state, independent of the risk';
COMMENT ON COLUMN analysis.sha256        IS 'Fingerprint of the .eml (lowercase hex). The .eml itself is not stored';
COMMENT ON COLUMN analysis.subject       IS 'Email subject, used to identify the email in the history. NULL when there is no subject. Untrusted data (written by the sender): render with textContent';
COMMENT ON COLUMN analysis.risk_score    IS 'Rules score 0-100 (not a probability). NULL when INCONCLUSIVE or not finished';
COMMENT ON COLUMN analysis.risk_level    IS 'RiskLevel: LOW | MEDIUM | HIGH | INCONCLUSIVE';
COMMENT ON COLUMN analysis.coverage      IS 'Coverage: FULL when every required check worked; PARTIAL when one failed or was skipped for lack of time';
COMMENT ON COLUMN analysis.findings      IS 'JSON: valid findings (SYSTEM and AI), discarded findings and limitations. Java guarantees it is valid JSON';
COMMENT ON COLUMN analysis.summary       IS 'AI explanation, or a template when the AI was not available';
COMMENT ON COLUMN analysis.rules_version IS 'RiskPolicy version that produced the result';
COMMENT ON COLUMN analysis.error         IS 'Failure reason (short message, no stack trace). Only when status = FAILED';

-- ---------------------------------------------------------------------
-- tool_call: every check or tool that was executed (the evidence)
-- ---------------------------------------------------------------------
CREATE TABLE tool_call (
    id          BIGSERIAL    PRIMARY KEY,
    analysis_id BIGINT       NOT NULL,
    tool_name   VARCHAR(60)  NOT NULL,
    invoked_by  VARCHAR(10)  NOT NULL,
    input       TEXT,
    output      TEXT,
    success     BOOLEAN      NOT NULL,
    error       VARCHAR(500),
    duration_ms BIGINT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_tool_call_analysis FOREIGN KEY (analysis_id)
        REFERENCES analysis (id) ON DELETE CASCADE,

    -- Enum
    CONSTRAINT chk_tool_call_invoked_by CHECK (invoked_by IN ('SYSTEM', 'AGENT')),

    -- Consistency: error exists if and only if the tool failed
    CONSTRAINT chk_tool_call_error CHECK (
        (success AND error IS NULL)
        OR
        (NOT success AND error IS NOT NULL)
    ),
    CONSTRAINT chk_tool_call_duration CHECK (duration_ms IS NULL OR duration_ms >= 0)
);

-- Supports the foreign key and reading the timeline in order (WHERE analysis_id = ? ORDER BY id)
CREATE INDEX idx_tool_call_analysis ON tool_call (analysis_id, id);

COMMENT ON TABLE  tool_call            IS 'Timeline: every required check (SYSTEM) or agent tool call (AGENT)';
COMMENT ON COLUMN tool_call.tool_name  IS 'headerAnalysis, extractLinks, bodyText, rdapLookup, ipInfo, reputation, investigateDomain';
COMMENT ON COLUMN tool_call.invoked_by IS 'InvokedBy: SYSTEM (required, run by Java) | AGENT (chosen by the agent)';
COMMENT ON COLUMN tool_call.output     IS 'Raw tool output, truncated (about 20 KB). Untrusted data';
COMMENT ON COLUMN tool_call.success    IS 'No DEFAULT: the backend always sets the result explicitly';
COMMENT ON COLUMN tool_call.error      IS 'Required when success = false; NULL when success = true';
