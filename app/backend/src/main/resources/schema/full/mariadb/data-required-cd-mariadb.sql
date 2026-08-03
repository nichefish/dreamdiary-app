-- 코드 데이터 쿼리 정보를 입력한다.
-- 쿼리 줄바꿈 안됨. 무조건 한 줄에 한 쿼리 단위로 실행된다.
-- 다국어 규약 :: code_item.code_name = 한국어 기본값(ko). ko 외 locale 명칭은 code_item_i18n 에 넣는다. (CodeLookupItem 계약과 동일 — ko 는 i18n 에 넣지 않음)
-- 각 그룹 섹션은 [코드 그룹 → 상세 코드(ko) → 영문(en) 명칭] 순으로 함께 관리한다. 코드 추가 시 en 명칭도 같이 추가할 것.
-- 재실행 안전 :: code_item 은 uk(group_code, code), code_item_i18n 은 PK(code_item_id, locale) 기준 INSERT IGNORE.
-- @database : mariadb
-- @author : nichefish

-- -------------------

-- 정합 :: 레거시 일정 코드 명칭을 현행 코드로 수렴한다. (SCHDUL_CD → SCHEDULE_CD, HLDY → HOLYDAY, content_type schdul → SCHEDULE)
-- 반드시 SCHEDULE_CD INSERT 보다 먼저 실행한다. (INSERT 가 먼저 돌면 rename 이 uk(group_code, code) 충돌)
-- 공휴일 레거시(HLDY) 행 중 HOLYDAY 행과 날짜·제목이 완전 중복인 것은 rename 대신 삭제한다. (중복 표시 방지)
DELETE FROM schedule WHERE schedule_cd = 'HLDY' AND EXISTS (SELECT 1 FROM (SELECT bgn_dt, title FROM schedule WHERE schedule_cd = 'HOLYDAY') n WHERE n.bgn_dt = schedule.bgn_dt AND n.title = schedule.title);
UPDATE schedule SET schedule_cd = 'HOLYDAY' WHERE schedule_cd = 'HLDY';
UPDATE schedule SET content_type = 'SCHEDULE' WHERE content_type = 'schdul';
UPDATE code_group SET group_code = 'SCHEDULE_CD' WHERE group_code = 'SCHDUL_CD' AND NOT EXISTS (SELECT 1 FROM (SELECT group_code FROM code_group WHERE group_code = 'SCHEDULE_CD') g);
UPDATE code_item SET group_code = 'SCHEDULE_CD' WHERE group_code = 'SCHDUL_CD' AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM code_item ci2 WHERE ci2.group_code = 'SCHEDULE_CD') x);
UPDATE code_item SET code = 'HOLYDAY' WHERE group_code = 'SCHEDULE_CD' AND code = 'HLDY';

-- -------------------

-- 옵션 :: 분류 코드 분류 코드 추가
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('CL_CTGR_CD', '분류코드 분류 코드', '분류코드 분류 코드');
-- 필수 :: 분류 코드 분류 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('CL_CTGR_CD', 'SYS', '시스템', '시스템에서 필수적으로 사용되는 코드입니다.', '1');
-- 분류 코드 분류 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'System' FROM code_item WHERE group_code = 'CL_CTGR_CD' AND code = 'SYS';

-- -------------------

-- 필수 :: 수정권한 분류 코드 추가
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('MDFABLE_CD', '수정권한', '수정권한 코드');
-- 필수 :: 수정권한 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('MDFABLE_CD', 'REGSTR', '등록자', '등록자만 수정 가능', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('MDFABLE_CD', 'MNGR', '관리자', '관리자만 수정 가능', '2');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('MDFABLE_CD', 'USER', '사용자', '사용자만 수정 가능', '3');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('MDFABLE_CD', 'ALL', '전체', '전체 수정 가능', '99');
-- 수정권한 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Author' FROM code_item WHERE group_code = 'MDFABLE_CD' AND code = 'REGSTR';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Manager' FROM code_item WHERE group_code = 'MDFABLE_CD' AND code = 'MNGR';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'User' FROM code_item WHERE group_code = 'MDFABLE_CD' AND code = 'USER';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'All' FROM code_item WHERE group_code = 'MDFABLE_CD' AND code = 'ALL';

-- -----------------------

-- 옵션 :: 일정 분류 코드 추가
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('SCHEDULE_CD', '일정', '일정 코드');
-- 필수 :: 일정 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'HOLYDAY', '공휴일', '공휴일', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'CEREMONY', '행사', '행사', '2');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'BRTHDY', '생일', '생일', '3');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'INDT', '내부일정', '내부일정', '11');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'TLCMMT', '재택', '재택', '21');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'OUTDT', '외근', '외근', '22');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'VCATN', '휴가', '휴가', '23');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('SCHEDULE_CD', 'ETC', '기타', '기타', '99');
-- 일정 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Public holiday' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'HOLYDAY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Event' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'CEREMONY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Birthday' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'BRTHDY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Internal' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'INDT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Remote work' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'TLCMMT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Out of office' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'OUTDT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Vacation' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'VCATN';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Etc.' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'ETC';

-- -----------------------

-- 필수 :: 저널 결산 구분 분류 코드 추가
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('JOURNAL_ANNUAL_TY_CD', '저널 결산 구분', '저널 결산 구분 코드');
-- 필수 :: 저널 결산 구분 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_ANNUAL_TY_CD', 'DIARY', '일기', '일기 결산', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JOURNAL_ANNUAL_TY_CD', 'DREAM', '꿈', '꿈 결산', '2');
-- 저널 결산 구분 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Diary' FROM code_item WHERE group_code = 'JOURNAL_ANNUAL_TY_CD' AND code = 'DIARY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Dream' FROM code_item WHERE group_code = 'JOURNAL_ANNUAL_TY_CD' AND code = 'DREAM';

-- -----------------------

-- text class code group
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('TEXT_CLASS_CD', 'Text Class', 'text semantic code');
-- text class detail codes (코드명 자체가 영문이므로 en 명칭은 추가하지 않는다)
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

-- 필수 :: 권한 분류 코드 추가 (기존 DB 에만 있던 그룹 — 시드 커버리지 정합)
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('AUTH_CD', '권한 분류 코드', '권한 분류 코드');
-- 필수 :: 권한 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('AUTH_CD', 'USER', '사용자', '사용자', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('AUTH_CD', 'MNGR', '관리자', '관리자', '2');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('AUTH_CD', 'DEV', '개발자', '개발자', '3');
-- 권한 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'User' FROM code_item WHERE group_code = 'AUTH_CD' AND code = 'USER';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Manager' FROM code_item WHERE group_code = 'AUTH_CD' AND code = 'MNGR';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Developer' FROM code_item WHERE group_code = 'AUTH_CD' AND code = 'DEV';

-- -----------------------

-- 필수 :: 게시판 정의 예약 코드 추가 (기존 DB 에 code_group 행 없이 code_item 만 있던 고아 그룹 — 그룹 행 포함 정합)
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('BOARD_DEF_RSRVD_CD', '게시판 예약 코드', '게시판 정의 예약 코드');
-- 필수 :: 게시판 정의 예약 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('BOARD_DEF_RSRVD_CD', 'SCHDUL', '일정', '일정', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('BOARD_DEF_RSRVD_CD', 'JRNL_DAY', '저널 일자', '저널 일자', '4');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('BOARD_DEF_RSRVD_CD', 'JRNL_DIARY', '저널 일기', '저널 일기', '5');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('BOARD_DEF_RSRVD_CD', 'JRNL_DREAM', '저널 꿈', '저널 꿈', '6');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('BOARD_DEF_RSRVD_CD', 'COMMENT', '댓글', '댓글', '7');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('BOARD_DEF_RSRVD_CD', 'FLSYS_META', '파일시스템 메타', '파일시스템 메타', '8');
-- 게시판 정의 예약 코드 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Schedule' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'SCHDUL';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Journal day' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'JRNL_DAY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Journal diary' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'JRNL_DIARY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Journal dream' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'JRNL_DREAM';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Comment' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'COMMENT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Filesystem meta' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'FLSYS_META';

-- -----------------------

-- 옵션 :: 잔디 토픽 코드 추가 (기존 DB 에만 있던 그룹 — 시드 커버리지 정합)
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('JANDI_TOPIC_CD', '잔디 토픽', '잔디 토픽 코드');
-- 옵션 :: 잔디 토픽 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JANDI_TOPIC_CD', 'NOTICE', '공지사항', '공지사항', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JANDI_TOPIC_CD', 'SCHDUL', '일정공유', '일정공유', '2');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('JANDI_TOPIC_CD', 'TEST', '테스트', '테스트', '3');
-- 잔디 토픽 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Notice' FROM code_item WHERE group_code = 'JANDI_TOPIC_CD' AND code = 'NOTICE';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Schedule sharing' FROM code_item WHERE group_code = 'JANDI_TOPIC_CD' AND code = 'SCHDUL';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Test' FROM code_item WHERE group_code = 'JANDI_TOPIC_CD' AND code = 'TEST';

-- -----------------------

-- -----------------------

-- 옵션 :: 휴가 구분 코드 추가 (기존 DB 에 code_group 행 없이 code_item 만 있던 고아 그룹 — 그룹 행 포함 정합)
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('VCATN_CD', '휴가 구분', '휴가 구분 코드');
-- 옵션 :: 휴가 구분 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('VCATN_CD', 'ANNUAL', '연차', '연차', '0');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('VCATN_CD', 'AM_HALF', '오전반차', '오전반차', '1');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('VCATN_CD', 'PM_HALF', '오후반차', '오후반차', '2');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('VCATN_CD', 'PBLEN', '공가', '공가', '3');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('VCATN_CD', 'CTSNN', '경조휴가', '경조휴가', '4');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('VCATN_CD', 'MNSTR', '생리휴가', '생리휴가', '5');
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('VCATN_CD', 'UNPAID', '무급휴가', '무급휴가', '6');
-- 휴가 구분 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Annual leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'ANNUAL';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Morning half-day' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'AM_HALF';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Afternoon half-day' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'PM_HALF';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Public leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'PBLEN';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Family event leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'CTSNN';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Menstrual leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'MNSTR';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Unpaid leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'UNPAID';

-- -----------------------

-- 옵션 :: 지출 구분 코드 추가 (기존 DB 에 code_group 행 없이 code_item 만 있던 고아 그룹 — 그룹 행 포함 정합)
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('EXPTR_CD', '지출 구분', '지출 구분 코드');
-- 옵션 :: 지출 구분 상세 코드 추가
INSERT IGNORE INTO code_item (group_code, code, code_name, description, sort_order) VALUES ('EXPTR_CD', 'TRVL', '여비교통비', '여비교통비', '1');
-- 지출 구분 영문(en) 명칭 추가
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Travel expenses' FROM code_item WHERE group_code = 'EXPTR_CD' AND code = 'TRVL';
