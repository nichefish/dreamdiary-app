package io.nicheblog.dreamdiary.feature.journal.setting.model;

import io.nicheblog.dreamdiary.feature.journal.setting.type.JournalDefaultEntryView;
import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * JournalUserSettingDto
 * <pre>
 *  로그인 사용자에게 적용되는 저널 설정 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalUserSettingDto {

    /** 저널 공통 진입점에서 이동할 기본 화면. */
    @NotNull
    private JournalDefaultEntryView defaultEntryView;
}
