-- journal_setting.embedding_enabled → ai_enabled 컬럼 rename.
-- AI 게이트가 임베딩(embedding)과 개체추출(entity catalog) 파이프라인을 함께
-- 제어하므로, 임베딩 전용으로 읽히던 컬럼명을 AI 기능 일반으로 정합화한다.
ALTER TABLE journal_setting
    CHANGE COLUMN embedding_enabled ai_enabled TINYINT(1) NOT NULL DEFAULT 1
        COMMENT 'AI 기능 활성화 여부 (임베딩·개체추출, 1=ON, 0=OFF)';