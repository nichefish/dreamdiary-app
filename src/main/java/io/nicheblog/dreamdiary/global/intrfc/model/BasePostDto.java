package io.nicheblog.dreamdiary.global.intrfc.model;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * BasePostDto
 * <pre>
<<<<<<< HEAD
 *  (공통/상속) 게시판 Dto.
=======
 *  (공통/상속) 일반게시판 Dto.
>>>>>>> be8337fa (패키지 구조 대규모 개편.)
 *  "All classes in the hierarchy must be annotated with @SuperBuilder."
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
public class BasePostDto
        extends BaseClsfDto {

    /** 제목 */
    protected String title;

    /** 내용 */
    protected String cn;

    /** 마크다운 처리된 내용 */
    protected String markdownCn;

    /** 글분류 코드 */
    @Size(max = 50)
    protected String ctgrClCd;

    /** 글분류 코드 */
    @Size(max = 50)
    protected String ctgrCd;

    /** 글분류 코드 이름 */
    @Size(max = 50)
    protected String ctgrNm;

    /** 글분류 존재 여부 */
    @Builder.Default
    protected Boolean hasCtgrNm = false;
    
    /** 중요 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    protected String imprtcYn = "N";

    /** 상단고정 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    protected String fxdYn = "N";

    /** 조회수 */
    @Builder.Default
    @Min(value = 0)
    protected Integer hitCnt = 0;

    /** 수정권한 */
    @Builder.Default
    @Size(max = 50)
    protected String mdfable = Constant.MDFABLE_REGSTR;

    /** 수정 가능 여부 */
    @Builder.Default
    protected Boolean isMdfable = false;
}
