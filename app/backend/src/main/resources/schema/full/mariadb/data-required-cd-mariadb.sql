-- 코드 데이터 쿼리 정보를 입력한다.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 옵션 :: 분류 코드 분류 코드 추가
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('CL_CTGR_CD', '분류코드 분류 코드', '분류코드 분류 코드');
-- 필수 :: 분류 코드 분류 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('CL_CTGR_CD', 'SYS', '시스템', '시스템에서 필수적으로 사용되는 코드입니다.', '1');

-- -------------------

-- 필수 :: 수정권한 분류 코드 추가
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('MDFABLE_CD', '수정권한', '수정권한 코드');
-- 필수 :: 수정권한 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('MDFABLE_CD', 'REGSTR', '등록자', '등록자만 수정 가능', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('MDFABLE_CD', 'MNGR', '관리자', '관리자만 수정 가능', '2');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('MDFABLE_CD', 'USER', '사용자', '사용자만 수정 가능', '3');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('MDFABLE_CD', 'ALL', '전체', '전체 수정 가능', '99');

-- -----------------------

-- 옵션 :: 일정 분류 코드 추가
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('SCHEDULE_CD', '일정', '일정 코드');
-- 필수 :: 수정권한 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'HOLYDAY', '공휴일', '공휴일', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'CEREMONY', '행사', '행사', '2');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'BRTHDY', '생일', '생일', '3');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'INDT', '내부일정', '내부일정', '11');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'TLCMMT', '재택', '재택', '21');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'OUTDT', '외근', '외근', '22');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'VCATN', '휴가', '휴가', '23');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'ETC', '기타', '기타', '99');

-- -----------------------

-- 필수 :: 저널 결산 구분 분류 코드 추가
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('JOURNAL_ANNUAL_TY_CD', '저널 결산 구분', '저널 결산 구분 코드');
-- 필수 :: 저널 결산 구분 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_ANNUAL_TY_CD', 'DIARY', '일기', '일기 결산', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_ANNUAL_TY_CD', 'DREAM', '꿈', '꿈 결산', '2');

-- -----------------------

-- text class code group
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('TEXT_CLASS_CD', 'Text Class', 'text semantic code');
-- text class detail codes
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'DEFAULT', 'DEFAULT', '', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'SUCCESS', 'SUCCESS', 'text-success', '2');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'INFO', 'INFO', 'text-account', '3');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'WARNING', 'WARNING', 'text-warning', '4');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'DANGER', 'DANGER', 'text-danger', '5');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'PRIMARY', 'PRIMARY', 'text-primary', '6');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'SECONDARY', 'SECONDARY', 'text-secondary', '7');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'DARK', 'DARK', 'text-dark', '8');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'MUTED', 'MUTED', 'text-muted', '9');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'DIALOG', 'DIALOG', 'text-dialog', '10');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'NOTI', 'NOTI', 'text-noti', '11');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'BURNT', 'BURNT', 'text-burnt', '12');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('TEXT_CLASS_CD', 'EMOTION', 'EMOTION', 'text-emotion', '13');


-- -----------------------

-- 저널 챕터 일기/노트 전용 글분류 코드 그룹 추가 (기존 JOURNAL_CHAPTER_CTGR_CD 에서 분리)
-- 기존 JOURNAL_CHAPTER_CTGR_CD 항목은 모두 DIARY 챕터용이었으므로 DIARY 그룹으로 복사한다.
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('JOURNAL_CHAPTER_DIARY_CTGR_CD', '저널 챕터 일기 글분류', '저널 챕터 일기 전용 글분류 코드');
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('JOURNAL_CHAPTER_NOTE_CTGR_CD', '저널 챕터 노트 글분류', '저널 챕터 노트 전용 글분류 코드');

-- 저널 챕터 일기 전용 글분류 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_DIARY_CTGR_CD', 'SUMMARY',     '요약',     '요약',     0);
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_DIARY_CTGR_CD', 'INTERACTION', '관계',     '관계',     1);
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_DIARY_CTGR_CD', 'FAMILY',      '가족',     '가족',     3);
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_DIARY_CTGR_CD', 'EMOTION',     '감정',     '감정',     4);
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_DIARY_CTGR_CD', 'EXCERPT',     '발췌',     '발췌',     5);
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_DIARY_CTGR_CD', 'DYNAMICS',    '회사역동', '회사역동', 4);
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_DIARY_CTGR_CD', 'ETC',         '기타',     '기타',     6);

-- 저널 챕터 노트 전용 글분류 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_NOTE_CTGR_CD', 'EXCERPT', '발췌',   '발췌',   1);
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_NOTE_CTGR_CD', 'REVIEW',  '리뷰',   '리뷰',   2);
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_CHAPTER_NOTE_CTGR_CD', 'PLAY',    '플레이', '플레이', 3);

-- 기존 JOURNAL_CHAPTER_CTGR_CD 항목 및 그룹을 제거한다.
DELETE FROM code_item  WHERE group_code = 'JOURNAL_CHAPTER_CTGR_CD';
DELETE FROM code_group WHERE group_code = 'JOURNAL_CHAPTER_CTGR_CD';
