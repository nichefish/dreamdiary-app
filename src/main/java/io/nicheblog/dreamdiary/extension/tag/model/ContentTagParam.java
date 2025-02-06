package io.nicheblog.dreamdiary.extension.tag.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * ContentTagParam
 * <pre>
 *  컨텐츠 태그 목록 검색 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
public class ContentTagParam {

    /** 참조 글 번호 */
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    private String refContentType;

    /** 태그 이름 */
    private String tagNm;

    /** 카테고리 */
    private String ctgr;

    /** 등록자 ID */
    private String regstrId;
}
