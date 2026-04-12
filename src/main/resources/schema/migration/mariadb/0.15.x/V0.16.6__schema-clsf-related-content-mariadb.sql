-- 관련글 테이블 추가
-- @database : mariadb
-- @author : nichefish

CREATE TABLE IF NOT EXISTS related_content (
    related_content_no INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '관련글 번호 (PK)',
    left_post_no INT NOT NULL COMMENT '좌측 글 번호',
    left_content_type VARCHAR(30) NOT NULL COMMENT '좌측 컨텐츠 타입',
    right_post_no INT NOT NULL COMMENT '우측 글 번호',
    right_content_type VARCHAR(30) NOT NULL COMMENT '우측 컨텐츠 타입',
    relation_type VARCHAR(30) NOT NULL COMMENT '관계 타입',
    reason VARCHAR(255) COMMENT '관계 사유',
    origin_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '관계 생성 출처',
    regstr_id VARCHAR(20) COMMENT '등록자 ID',
    reg_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    del_yn CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    UNIQUE KEY uk_related_content_pair (left_content_type, left_post_no, right_content_type, right_post_no, regstr_id),
    INDEX idx_related_content_left (left_post_no, left_content_type, regstr_id),
    INDEX idx_related_content_right (right_post_no, right_content_type, regstr_id),
    INDEX idx_related_content_type (relation_type),
    INDEX idx_related_content_origin (origin_type)
) COMMENT = '관련글';
