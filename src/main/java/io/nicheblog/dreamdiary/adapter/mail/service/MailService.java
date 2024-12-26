package io.nicheblog.dreamdiary.adapter.mail.service;

import io.nicheblog.dreamdiary.adapter.mail.model.MailAddress;
import io.nicheblog.dreamdiary.adapter.mail.model.MailSendParam;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * MailService
 * <pre>
 *  이메일 관련 처리 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface MailService {

    /**
     * 메일 발송
     *
     * @param mailSendParam 메일 발송에 필요한 정보가 담긴 MailSendParam 객체
     * @return {@link Boolean} -- 메일 발송 시도 결과
     */
    public Boolean send(MailSendParam mailSendParam);

    /**
     * MIME 메시지를 생성합니다.
     *
     * @param mailSendParam 메일 발송에 필요한 정보가 담긴 MailSendParam 객체
     * @return 생성된 MimeMessage 객체
     * @throws Exception 메시지 생성 중 발생할 수 있는 예외
     */
    MimeMessage createMimeMessage(MailSendParam mailSendParam) throws Exception;

    /**
     * 발신자를 설정합니다.
     *
     * @param helper MIME 메시지 도우미 객체
     * @param sender 발신자 정보가 담긴 MailAddress 객체
     * @throws MessagingException 메시지 설정 중 발생할 수 있는 예외
     * @throws UnsupportedEncodingException 지원되지 않는 인코딩이 발견될 경우 발생하는 예외
     */
    void setSender(final MimeMessageHelper helper, MailAddress sender) throws MessagingException, UnsupportedEncodingException;

    /**
     * 수신자를 설정합니다.
     *
     * @param helper MIME 메시지 도우미 객체
     * @param recipientList 수신자 정보가 담긴 MailAddress 객체의 리스트
     * @throws MessagingException 수신자 설정 중 발생할 수 있는 예외
     */
    void setRecipients(MimeMessageHelper helper, List<MailAddress> recipientList) throws MessagingException, UnsupportedEncodingException;
}
