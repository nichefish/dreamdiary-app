-- 일정 코드 레거시 정합 + 고아 code_group 행 보강 + 상세 코드 영문(en) 명칭 추가
-- (data-required-cd-mariadb.sql 과 동일 계약 — 라이브 DB 반영용 마이그레이션)
-- @database : mariadb
-- @author : nichefish

-- 정합 :: 레거시 일정 코드 명칭을 현행 코드로 수렴한다. (SCHDUL_CD → SCHEDULE_CD, HLDY → HOLYDAY, content_type schdul → SCHEDULE)
-- 공휴일 레거시(HLDY) 행 중 HOLYDAY 행과 날짜·제목이 완전 중복인 것은 rename 대신 삭제한다. (중복 표시 방지)
DELETE FROM schedule WHERE schedule_cd = 'HLDY' AND EXISTS (SELECT 1 FROM (SELECT bgn_dt, title FROM schedule WHERE schedule_cd = 'HOLYDAY') n WHERE n.bgn_dt = schedule.bgn_dt AND n.title = schedule.title);
UPDATE schedule SET schedule_cd = 'HOLYDAY' WHERE schedule_cd = 'HLDY';
UPDATE schedule SET content_type = 'SCHEDULE' WHERE content_type = 'schdul';
UPDATE code_group SET group_code = 'SCHEDULE_CD' WHERE group_code = 'SCHDUL_CD' AND NOT EXISTS (SELECT 1 FROM (SELECT group_code FROM code_group WHERE group_code = 'SCHEDULE_CD') g);
UPDATE code_item SET group_code = 'SCHEDULE_CD' WHERE group_code = 'SCHDUL_CD' AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM code_item ci2 WHERE ci2.group_code = 'SCHEDULE_CD') x);
UPDATE code_item SET code = 'HOLYDAY' WHERE group_code = 'SCHEDULE_CD' AND code = 'HLDY';

-- 정합 :: code_item 만 있고 code_group 행이 없던 고아 그룹에 그룹 행을 추가한다.
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('BOARD_DEF_RSRVD_CD', '게시판 예약 코드', '게시판 정의 예약 코드');
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('VCATN_CD', '휴가 구분', '휴가 구분 코드');
INSERT IGNORE INTO code_group (group_code, group_name, description) VALUES ('EXPTR_CD', '지출 구분', '지출 구분 코드');

-- 다국어 :: 상세 코드 영문(en) 명칭 추가 (code_item_i18n — PK(code_item_id, locale) 기준 INSERT IGNORE, 재실행 무해)
-- TEXT_CLASS_CD 는 코드명 자체가 영문이므로 제외한다.
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'User' FROM code_item WHERE group_code = 'AUTH_CD' AND code = 'USER';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Manager' FROM code_item WHERE group_code = 'AUTH_CD' AND code = 'MNGR';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Developer' FROM code_item WHERE group_code = 'AUTH_CD' AND code = 'DEV';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Schedule' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'SCHDUL';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Journal day' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'JRNL_DAY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Journal diary' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'JRNL_DIARY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Journal dream' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'JRNL_DREAM';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Comment' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'COMMENT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Filesystem meta' FROM code_item WHERE group_code = 'BOARD_DEF_RSRVD_CD' AND code = 'FLSYS_META';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'System' FROM code_item WHERE group_code = 'CL_CTGR_CD' AND code = 'SYS';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Travel expenses' FROM code_item WHERE group_code = 'EXPTR_CD' AND code = 'TRVL';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Notice' FROM code_item WHERE group_code = 'JANDI_TOPIC_CD' AND code = 'NOTICE';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Schedule sharing' FROM code_item WHERE group_code = 'JANDI_TOPIC_CD' AND code = 'SCHDUL';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Test' FROM code_item WHERE group_code = 'JANDI_TOPIC_CD' AND code = 'TEST';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Diary' FROM code_item WHERE group_code = 'JOURNAL_ANNUAL_TY_CD' AND code = 'DIARY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Dream' FROM code_item WHERE group_code = 'JOURNAL_ANNUAL_TY_CD' AND code = 'DREAM';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Summary' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_DIARY_CTGR_CD' AND code = 'SUMMARY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Interaction' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_DIARY_CTGR_CD' AND code = 'INTERACTION';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Workplace dynamics' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_DIARY_CTGR_CD' AND code = 'DYNAMICS';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Family' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_DIARY_CTGR_CD' AND code = 'FAMILY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Emotion' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_DIARY_CTGR_CD' AND code = 'EMOTION';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Excerpt' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_DIARY_CTGR_CD' AND code = 'EXCERPT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Etc.' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_DIARY_CTGR_CD' AND code = 'ETC';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Excerpt' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_NOTE_CTGR_CD' AND code = 'EXCERPT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Review' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_NOTE_CTGR_CD' AND code = 'REVIEW';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Play' FROM code_item WHERE group_code = 'JOURNAL_CHAPTER_NOTE_CTGR_CD' AND code = 'PLAY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Finance' FROM code_item WHERE group_code = 'JOURNAL_THREAD_CTGR_CD' AND code = 'FNNC';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Career' FROM code_item WHERE group_code = 'JOURNAL_THREAD_CTGR_CD' AND code = 'JOB';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Life' FROM code_item WHERE group_code = 'JOURNAL_THREAD_CTGR_CD' AND code = 'LIFE';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Reflection' FROM code_item WHERE group_code = 'JOURNAL_THREAD_CTGR_CD' AND code = 'RFLX';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Author' FROM code_item WHERE group_code = 'MDFABLE_CD' AND code = 'REGSTR';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Manager' FROM code_item WHERE group_code = 'MDFABLE_CD' AND code = 'MNGR';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'User' FROM code_item WHERE group_code = 'MDFABLE_CD' AND code = 'USER';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'All' FROM code_item WHERE group_code = 'MDFABLE_CD' AND code = 'ALL';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Public holiday' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'HOLYDAY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Event' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'CEREMONY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Birthday' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'BRTHDY';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Internal' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'INDT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Remote work' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'TLCMMT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Out of office' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'OUTDT';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Vacation' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'VCATN';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Etc.' FROM code_item WHERE group_code = 'SCHEDULE_CD' AND code = 'ETC';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Annual leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'ANNUAL';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Morning half-day' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'AM_HALF';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Afternoon half-day' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'PM_HALF';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Public leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'PBLEN';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Family event leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'CTSNN';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Menstrual leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'MNSTR';
INSERT IGNORE INTO code_item_i18n (code_item_id, locale, code_name) SELECT id, 'en', 'Unpaid leave' FROM code_item WHERE group_code = 'VCATN_CD' AND code = 'UNPAID';
