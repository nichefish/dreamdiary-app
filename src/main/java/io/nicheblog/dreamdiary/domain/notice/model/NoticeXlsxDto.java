package io.nicheblog.dreamdiary.domain.notice.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * NoticeXlsxDto
 * <pre>
 *  공지사항 엑셀 다운로드 Dto.
 *  (필드 정의 후 XlsxUtils에서 속성을 reflection으로 읽어 동적으로 처리)
 *  (헤더:: XlsxConstant에 정의)
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode
@ToString(callSuper = true)
public class NoticeXlsxDto {

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.NOTICE.key;
}
