package io.nicheblog.dreamdiary.domain.admin.menu.model;

import lombok.RequiredArgsConstructor;

/**
 * ContentType
 * <pre>
 *  ClsfEntity를 상속받은 클래스들이 사용하는 컨텐츠 타입 정보
 * </pre>
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum PageNm {

    LGN("로그인"),
    MAIN("메인"),
    ERROR("에러"),
    LIST("목록 조회"),
    REG("등록"),
    DTL("상세 조회"),
    MDF("수정"),
    POP("팝업 조회"),
    STATS("통계 조회"),
    CAL("달력 조회"),
    PREVIEW("미리보기"),

    PAGE("페이지"),
    DEFAULT("");

    public final String pageNm;
}