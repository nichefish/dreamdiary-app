package io.nicheblog.dreamdiary.global.model;

import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * SiteAcsInfo
 * <pre>
 *  메뉴 및 화면 정보 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
@Getter
@Setter
@NoArgsConstructor
public class SiteAcsInfo {

    /** 사이드바 메뉴 번호 */
    private String menuNo;

    /** 상위 메뉴 유형 코드 */
    private String upperMenuTyCd;
    /** 상위 메뉴 번호 (메뉴 활성화용) */
    private String upperMenuNo;
    /** 상위 메뉴 이름 (툴바 표시용) */
    private String upperMenuNm;

    /** 게시판 코드 */
    private String boardDef;
    /** 사이드바 메뉴 이름 (로깅 및 사이트 헤더 표시용) */
    private String menuNm;

    /** 접근 화면 이름 (로깅 및 사이트 헤더 표시용) */
    private String pageNm;

    /** 관리자 메뉴 여부 */
    private Boolean isMngrMenu = false;
    /** 사이드메뉴 표시 여부 */
    private Boolean asideAt = true;

    /* ----- */

    /** 페이지명 세팅 (for breadcrumb) */
    public void setAcsPageInfo(final PageNm pageNm) {
        this.pageNm = pageNm.pageNm;
    }
}
