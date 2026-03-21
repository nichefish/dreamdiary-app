package io.nicheblog.dreamdiary.global.intrfc.model.param;

import io.nicheblog.dreamdiary.feature.clsf.shared.model.param.BaseClsfSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * BasePostSearchParam
 * <pre>
<<<<<<< HEAD
 *  (공통/상속) 게시물 목록 검색 파라미터.
=======
 *  (공통/상속) 게시물 목록 검색 파라미터 Dto.
>>>>>>> be8337fa (패키지 구조 대규모 개편.)
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BasePostSearchParam
        extends BaseClsfSearchParam {

    /** 제목 */
    protected String title;

    /** 내용 */
    protected String cn;

    /** 글분류 코드 */
    protected String ctgrCd;

    /** 중요 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    protected String imprtcYn;

    /** 상단고정 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    protected String fxdYn;
}
