package io.nicheblog.dreamdiary.extension.cd;

import lombok.AllArgsConstructor;

/**
 * CdConstant
 * <pre>
 *  공통으로 사용하는 코드성 데이터 정의
 * </pre>
 * TODO: enum으로 점진적 변환쓰
 *
 * @author nichefish
 */
public interface CdConstant {

    /** 디바이스 정보 */
    @AllArgsConstructor
    enum Device {
        PC,
        MOBILE,
        TABLET;
    }
    String DVC_PC = Device.PC.name();
    String DVC_MOBILE = Device.MOBILE.name();
    String DVC_TABLET = Device.TABLET.name();

    /** 분류 분류 코드 */
    String CL_CTGR_CD = "CL_CTGR_CD";

    /** 하위메뉴 확장 유형 코드 */
    String MENU_SUB_EXTEND_TY_CD = "MENU_SUB_EXTEND_TY_CD";
    @AllArgsConstructor
    enum MenuSubExtendTy {
        EXTEND("우측으로 확장"),
        LIST("하단에 목록 표시"),
        NO_SUB("하위메뉴 없음");

        public final String desc;
    }

    /** 소속(팀) 코드 */
    String TEAM_CD = "TEAM_CD";

    /** 재직 구분 코드 */
    @AllArgsConstructor
    enum Emplym {
        EMPLYM("재직"),
        FREE("프리랜서");

        public final String desc;
    }
    String EMPLYM_CD = "EMPLYM_CD";
    String EMPLYM_FREE = Emplym.FREE.name();

    @AllArgsConstructor
    enum Rank {
        INTN("인턴"),
        STAFF("사원"),
        DAERI("대리"),
        GWJANG("과장"),
        CHJANG("차장"),
        BJANG("부장"),
        SLJANG("실장"),
        DRCTR("이사"),
        PRSDNT("사장");

        public final String desc;
    }
    String RANK_CD = "JOB_TITLE_CD";       // 직급 코드
    String RANK_STAFF = Rank.STAFF.name();           // 직급:사원

    String NOTICE_CTGR_CD = "NOTICE_CTGR_CD";   // 공지사항 글분류 코드
    String POST_CTGR_CD = "POST_CTGR_CD";       // 게시판
    String JANDI_TOPIC_CD = "JANDI_TOPIC_CD";   // 잔디 토픽 코드
    String JRNL_SBJCT_CTGR_CD = "JRNL_SBJCT_CTGR_CD";   // 저널 주제 글분류 코드

    String YY_CD = "YY_CD";                 // 사용자 권한 코드
    String MNTH_CD = "MNTH_CD";                 // 사용자 권한 코드

    String ACTVTY_CTGR_CD = "ACTVTY_CTGR_CD";     // 작업 카테고리 코드
    String ACTION_TY_CD = "ACTION_TY_CD";         // 액션 유형 코드

    /* 메뉴 분류 코드 */
    String MENU_TY_CD = "MENU_TY_CD";
    String MENU_TY_MAIN = "MAIN";
    String MENU_TY_SUB = "SUB";

    /** 꿈 결산 구분 코드 */
    String JRNL_SUMRY_TY_CD = "JRNL_SUMRY_TY_CD";
    @AllArgsConstructor
    enum JrnlSumryTy {
        DREAM("꿈"),
        DIARY("일기");

        public final String desc;
    }

    /** 일정 분류 코드 */
    @AllArgsConstructor
    enum Schdul {
        HLDY("공휴일"),
        CEREMONY("행사"),
        TLCMMT("재택근무"),
        OUTDT("외근"),
        INDT("내부일정"),
        VCATN("휴가"),
        BRTHDY("생일"),
        ETC("기타");

        public final String desc;
    }
    String SCHDUL_CD = "SCHDUL_CD";       // 일정 구분 코드
    String SCHDUL_HLDY = Schdul.HLDY.name();
    String SCHDUL_CEREMONY = Schdul.CEREMONY.name();
    String SCHDUL_TLCMMT = Schdul.TLCMMT.name();
    String SCHDUL_OUTDT = Schdul.OUTDT.name();
    String SCHDUL_INDT = Schdul.INDT.name();
    String SCHDUL_VCATN = Schdul.VCATN.name();
    String SCHDUL_BRTHDY = Schdul.BRTHDY.name();
    String SCHDUL_ETC = Schdul.ETC.name();

    /** 휴가 분류 코드 */
    @AllArgsConstructor
    enum Vcatn {
        ANNUAL("연차"),
        AM_HALF("오전반차"),
        PM_HALF("오후반차"),
        PBLEN("공가"),
        CTSNN("경조휴가"),
        UNPAID("무급휴가"),
        MNSTR("생리휴가");

        public final String desc;
    }
    String VCATN_CD = "VCATN_CD";
    String VCATN_ANNUAL = Vcatn.ANNUAL.name();
    String VCATN_AM_HALF = Vcatn.AM_HALF.name();
    String VCATN_PM_HALF = Vcatn.PM_HALF.name();
    String VCATN_PBLEN = Vcatn.PBLEN.name();
    String VCATN_CTSNN = Vcatn.CTSNN.name();
    String VCATN_UNPAID = Vcatn.UNPAID.name();
    String VCATN_MNSTR = Vcatn.MNSTR.name();

    /** 활동 구분 코드 (로그) */
    String ACTION_TY_SEARCH = "SEARCH";
    String ACTION_TY_MY_PAPR = "MY_PAPR";
    String ACTION_TY_VIEW = "VIEW";
    String ACTION_TY_SUBMIT = "SUBMIT";
    String ACTION_TY_DOWNLOAD = "DOWNLOAD";

    /** UTM 파라미터 코드 */
    @AllArgsConstructor
    enum UtmParam {
        UTM_SOURCE("utm_source", "등록자"),
        UTM_MEDIUM("utm_medium", "관리자"),
        UTM_CAMPAIGN("utm_campaign", "사용자"),
        UTM_TERM("utm_term", "사용자"),
        UTM_CONTENT( "utm_content", "전체");

        public final String key;
        public final String desc;
    }
    String UTM_SOURCE = UtmParam.UTM_SOURCE.key;
    String UTM_MEDIUM = UtmParam.UTM_MEDIUM.key;
    String UTM_CAMPAIGN = UtmParam.UTM_CAMPAIGN.key;
    String UTM_TERM = UtmParam.UTM_TERM.key;
    String UTM_CONTENT = UtmParam.UTM_CONTENT.key;

    /* 코드 정보 */

    /* 소속(회사) 코드 */
    String CMPY_CD = "CMPY_CD";

    @AllArgsConstructor
    enum CMPY {
        AA("aa"),
        BB("bb");

        public final String desc;
    }
}
