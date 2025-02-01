INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/lgnForm.do', '로그인 화면');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/lgnProc.do', '로그인 처리');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/lgnPwChgAjax.do', '로그인 패스워드 변경 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/lgout.do', '로그아웃');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/error', '에러 화면');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/error/errorPage.do', '에러 화면');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/error/notFound.do', '에러 화면 (404)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/error/accessDenied.do', '에러 화면 (400)');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/', '메인 화면');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/main.do', '메인 화면');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/admin/main.do', '메인 화면 (관리자)');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/notice/noticeList.do', '공지 목록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/notice/noticeRegForm.do', '공지 등록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/notice/noticeRegAjax.do', '공지 등록 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/notice/noticeDtl.do', '공지 상세 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/notice/noticeDtlAjax.do', '공지 상세 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/notice/noticeMdfForm.do', '공지 수정 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/notice/noticeMdfAjax.do', '공지 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/notice/noticeDelAjax.do', '공지 삭제 (Ajax)');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/post/boardPostList.do', '게시판 게시물 목록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/post/boardPostRegForm.do', '게시판 게시물 등록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/post/boardPostRegAjax.do', '게시판 게시물 등록 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/post/boardPostDtl.do', '게시판 게시물 상세 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/post/boardPostDtlAjax.do', '게시판 게시물 상세 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/post/boardPostMdfForm.do', '게시판 게시물 수정 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/post/boardPostMdfAjax.do', '게시판 게시물 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/post/boardPostDelAjax.do', '게시판 게시물 삭제 (Ajax)');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/comment/boardCommentListAjax.do', '댓글 목록 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/comment/boardCommentRegAjax.do', '댓글 등록 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/comment/boardCommentMdfAjax.do', '댓글 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/comment/boardCommentDelAjax.do', '댓글 삭제 (Ajax)');
-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/tag/boardTagListAjax.do', '태그 전체 목록 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/tag/boardTagDtlAjax.do', '태그 상세 조회 (Ajax)');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/def/boardDefList.do', '게시판 정의 목록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/def/boardDefRegAjax.do', '게시판 정의 등록 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/def/boardDefMdfItemAjax.do', '게시판 정의 항목 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/def/boardDefDelAjax.do', '게시판 정의 삭제 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/def/boardDefUseAjax.do', '게시판 정의 사용 처리 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/board/def/boardDefUnuseAjax.do', '게시판 정의 미사용 처리 (Ajax)');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/schdul/schdulCal.do', '일정 달력 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/schdul/schdulCalListAjax.do', '일정 달력 목록 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/schdul/schdulRegAjax.do', '일정 등록 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/schdul/schdulDtlAjax.do', '일정 상세 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/schdul/schdulMdfAjax.do', '일정 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/schdul/schdulDelAjax.do', '일정 삭제 (Ajax)');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprList.do', '휴가계획서 목록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprRegForm.do', '휴가계획서 등록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprRegAjax.do', '휴가계획서 등록 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprDtl.do', '휴가계획서 상세 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprDtlAjax.do', '휴가계획서 상세 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprMdfForm.do', '휴가계획서 수정 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprMdfAjax.do', '휴가계획서 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprDelAjax.do', '휴가계획서 삭제 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/papr/vcatnPaprCfAjax.do', '휴가계획서 승인 (Ajax)');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userList.do', '사용자 목록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userRegForm.do', '사용자 등록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userIdDupChckAjax.do', '사용자 ID 중복 체크 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userRegAjax.do', '사용자 등록 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userDtl.do', '사용자 상세 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userMdfForm.do', '사용자 수정 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userMdfAjax.do', '사용자 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userPwResetAjax.do', '사용자 패스워드 초기화 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userDelAjax.do', '사용자 삭제 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/userListXlsxDownload.do', '사용자 목록 엑셀 다운로드');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/my/myInfoDtl.do', '내 정보 상세 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/my/myPwCfAjax.do', '내 비밀번호 확인 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/user/my/myPwChgAjax.do', '내 비밀번호 변경 (Ajax)');

-- TODO: 인력 관리
-- TODO: 개발자 이력 관리

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/stats/vcatnStatsYy.do', '년도별 휴가관리 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/stats/vcatnStatsYyUpdtAjax.do', '년도별 휴가관리 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/stats/vcatnStatsYyXlsxDownload.do', '년도별 휴가관리 엑셀 다운로드');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/dy/vcatnSchdulList.do', '휴가사용일자 목록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/dy/vcatnSchdulRegAjax.do', '휴가사용일자 등록 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/dy/vcatnSchdulMdfAjax.do', '휴가사용일자 수정 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/dy/vcatnSchdulDtlAjax.do', '휴가사용일자 상세 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/dy/vcatnSchdulDelAjax.do', '휴가사용일자 삭제 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/vcatn/dy/vcatnSchdulXlsxDownload.do', '휴가사용일자 목록 엑셀 다운로드');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/file/fileUploadAjax.do', '파일 업로드');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/file/fileDownloadChck.do', '파일 다운로드 전 사전 체크');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/file/fileDownload.do', '파일 다운로드');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/admin/siteMng.do', '사이트 관리 화면 조회');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/cache/cacheActiveListAjax.do', '저장된 캐시 조회 (Ajax)');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/cache/cacheClearAjax.do', '저장된 캐시 초기화 (Ajax)');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/lgnPolicy/lgnPolicyForm.do', '로그인 관리 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/lgnPolicy/lgnPolicyRegAjax.do', '로그인 관리 화면 조회');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/cd/clCdList.do', '분류 코드 관리 목록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/cd/clCdDtl.do', '분류 코드 관리 상세 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/cd/clCdDtlAjax.do', '분류 코드 관리 상세 조회 (Ajax)');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/log/actvty/logActvtyList.do', '활동 로그 목록 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/log/actvty/logActvtyListXlsxDownload.do', '활동 로그 목록 엑셀 다운로드');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/log/sys/logSysList.do', '시스템 로그 목록 화면 조회');

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/log/stats/logStatsUserList.do', '사용자별 로그 통계 화면 조회');

-- -----------------------

INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/flsys/flsysHome.do', '파일 시스템 홈 화면 조회');
INSERT IGNORE INTO LOG_ACTVTY_URL_NM (URL, URL_NM) VALUES ('/flsys/flsysListAjax.do', '파일 시스템 파일 목록 조회 (Ajax)');



-- -----------------------

