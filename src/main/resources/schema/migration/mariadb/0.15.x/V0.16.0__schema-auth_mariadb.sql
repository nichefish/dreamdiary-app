-- refresh token columns (user)
ALTER TABLE user
    ADD COLUMN refresh_token_hash VARCHAR(64) COMMENT '리프레시 토큰 해시' AFTER password,
    ADD COLUMN refresh_token_issued_at DATETIME COMMENT '리프레시 토큰 발급일시' AFTER refresh_token_hash,
    ADD COLUMN refresh_token_expires_at DATETIME COMMENT '리프레시 토큰 만료일시' AFTER refresh_token_issued_at;
