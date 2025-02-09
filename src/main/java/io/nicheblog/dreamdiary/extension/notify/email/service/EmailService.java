package io.nicheblog.dreamdiary.extension.notify.email.service;

import io.nicheblog.dreamdiary.extension.notify.email.model.EmailAddress;
import io.nicheblog.dreamdiary.extension.notify.email.model.EmailSendParam;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * EmailService
 * <pre>
 *  이메일 관련 처리 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface EmailService {

    /**
     * 메일 발송
     *
     * @param mailSendParam 메일 발송에 필요한 정보가 담긴 EmailSendParam 객체
     * @return {@link Boolean} -- 메일 발송 시도 결과
     */
    public Boolean send(EmailSendParam mailSendParam);

    /**
     * MIME 메시지를 생성합니다.
     *
     * @param mailSendParam 메일 발송에 필요한 정보가 담긴 EmailSendParam 객체
     * @return 생성된 MimeMessage 객체
     * @throws Exception 메시지 생성 중 발생할 수 있는 예외
     */
    MimeMessage createMimeMessage(EmailSendParam mailSendParam) throws Exception;

    /**
     * 발신자를 설정합니다.
     *
     * @param helper MIME 메시지 도우미 객체
     * @param sender 발신자 정보가 담긴 EmailAddress 객체
     * @throws MessagingException 메시지 설정 중 발생할 수 있는 예외
     * @throws UnsupportedEncodingException 지원되지 않는 인코딩이 발견될 경우 발생하는 예외
     */
    void setSender(final MimeMessageHelper helper, EmailAddress sender) throws MessagingException, UnsupportedEncodingException;

    /**
     * 수신자를 설정합니다.
     *
     * @param helper MIME 메시지 도우미 객체
     * @param recipientList 수신자 정보가 담긴 EmailAddress 객체의 리스트
     * @throws MessagingException 수신자 설정 중 발생할 수 있는 예외
     */
    void setRecipients(MimeMessageHelper helper, List<EmailAddress> recipientList) throws MessagingException, UnsupportedEncodingException;
}
