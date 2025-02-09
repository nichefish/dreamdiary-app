package io.nicheblog.dreamdiary.extension.notify.email.model;

import io.nicheblog.dreamdiary.extension.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.global.Constant;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * EmailSendParam
 * <pre>
 *  메일 발송 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSendParam {
    
    /** 제목 */
    private String subject;

    /** 본문 템플릿 */
    private String tmplat;

    /** 발신자 (홍길동<test@gmail.com>) */
    @Builder.Default
    private EmailAddress sender = Constant.SYSTEM_EMAIL_ADDRESS;

    /** 수신자 Array(홍길동<test@gmail.com>) */
    private List<EmailAddress> recipientList;

    /** 첨부파일 목록 */
    private List<AtchFileDtlDto> atchFileList;

    /** 데이터:: freemarker 템플릿 처리 */
    private Map<String, Object> dataMap;

    /* ----- */

    /**
     * 생성자.
     *
     * @param tmplat 메일 템플릿을 나타내는 문자열
     */
    public EmailSendParam(final String tmplat) {
        this.tmplat = tmplat;
    }

    /**
     * 생성자.
     *
     * @param tmplat 메일 템플릿을 나타내는 문자열
     * @param sender 메일 발신자 정보
     */
    public EmailSendParam(final String tmplat, final EmailAddress sender) {
        this(tmplat);
        this.sender = sender;
    }
}
