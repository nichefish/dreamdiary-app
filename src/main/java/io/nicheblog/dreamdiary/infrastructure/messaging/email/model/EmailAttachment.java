package io.nicheblog.dreamdiary.infrastructure.messaging.email.model;

import io.nicheblog.dreamdiary.infrastructure.messaging.email.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EmailAttachment
 * <pre>
 *  인프라 이메일 모듈에서 사용하는 첨부파일 모델.
 * </pre>
 *
 * @author nichefish
 * @see EmailService
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailAttachment {

    /** 첨부파일의 원본 파일명 */
    private String fileNm;

    /** 첨부파일의 실제 저장 경로 */
    private String filePath;
}
