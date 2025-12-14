package io.nicheblog.dreamdiary.domain.user.info.model.profl;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;

/**
 * UserProflDto
 * <pre>
 *  사용자 프로필 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserProflDto
        extends BaseCrudDto {

    /** 사용자 정보 고유 ID (PK) */
    private Integer userProflNo;

    /** 생년월일 */
    private String brthdy;

    /** 음력 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String lunarYn = "N";

    /** 프로필 설명 */
    private String proflCn;
}
