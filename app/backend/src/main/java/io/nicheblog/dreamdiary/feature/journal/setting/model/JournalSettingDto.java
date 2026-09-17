package io.nicheblog.dreamdiary.feature.journal.setting.model;

import lombok.*;

/**
 * JournalSettingDto
 * <pre>
 *  저널 도메인 설정 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalSettingDto {

    /** AI 기능(임베딩·개체추출) 활성화 여부. */
    private Boolean aiEnabled;
}
