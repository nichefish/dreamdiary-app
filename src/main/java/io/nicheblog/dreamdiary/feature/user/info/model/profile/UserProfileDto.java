package io.nicheblog.dreamdiary.feature.user.info.model.profile;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;

/**
 * UserProfileDto
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
public class UserProfileDto extends BaseCrudDto {

    private Integer userProfileId;
    private String brthdy;

    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String lunarYn = "N";

    private String proflCn;
}
