package io.nicheblog.dreamdiary.extension.notify.email.service.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.nicheblog.dreamdiary.extension.notify.email.model.EmailAddress;
import io.nicheblog.dreamdiary.extension.notify.email.model.EmailSendParam;
import io.nicheblog.dreamdiary.extension.notify.email.service.EmailService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.extension.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.extension.log.sys.model.LogSysParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MailService
 * <pre>
 *  이메일 관련 처리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("mailService")
@RequiredArgsConstructor
@Log4j2
public class EmailServiceImpl
        implements EmailService {

    @Resource(name = "freemarkerEmailConfig")
    private Configuration freemarkerEmailConfig;

    private final JavaMailSender emailSender;
    private final ApplicationEventPublisher publisher;

    /**
     * 메일을 발송한다.
     *
     * @param mailSendParam 메일 발송에 필요한 정보가 담긴 MailSendParam 객체
     * @return {@link Boolean} -- 메일 발송 시도 결과
     * @see LogSysEventListener
     */
    @Override
    public Boolean send(EmailSendParam mailSendParam) {
        try {
            // 메세지 생성
            final MimeMessage message = this.createMimeMessage(mailSendParam);
            // 생성된 메세지 발송
            emailSender.send(message);
        } catch (final Exception e) {
            log.warn("email send failed", e);
            final LogSysParam logParam = new LogSysParam(true, "메일 발송에 실패했습니다.", ActvtyCtgr.SYSTEM);
            logParam.setExceptionInfo(e);
            publisher.publishEvent(new LogSysEvent(this, logParam));
        }
        return true;
    }

    /**
     * MIME 메시지를 생성한다.
     *
     * @param mailSendParam 메일 발송에 필요한 정보가 담긴 MailSendParam 객체
     * @return 생성된 MimeMessage 객체
     * @throws Exception 메시지 생성 중 발생할 수 있는 예외
     */
    @Override
    public MimeMessage createMimeMessage(EmailSendParam mailSendParam) throws Exception {
        final MimeMessage message = emailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, true, Constant.CHARSET_UTF_8);

        // 발신자 세팅 :: 메소드 분리
        this.setSender(helper, mailSendParam.getSender());
        // 제목
        helper.setSubject(mailSendParam.getSubject());
        // 수신자
        this.setRecipients(helper, mailSendParam.getRecipientList());
        // 내용 :: Freemarker 템플릿 처리
        final Map<String, Object> dataMap = mailSendParam.getDataMap();
        final Template tmplat = freemarkerEmailConfig.getTemplate(mailSendParam.getTmplat());
        final String compiledContent = FreeMarkerTemplateUtils.processTemplateIntoString(tmplat, dataMap);
        helper.setText(compiledContent, true);
        if (CollectionUtils.isNotEmpty(mailSendParam.getAtchFileList())) {
            // 첨부파일 처리
            mailSendParam.getAtchFileList().forEach(attachFile -> {
                try {
                    helper.addAttachment(attachFile.getOrgnFileNm(), new File(attachFile.getFileStrePath()));
                } catch (MessagingException e) {
                    log.error("Error attaching file", e);
                }
            });
        }

        return message;
    }

    /**
     * 발신자를 설정한다.
     *
     * @param helper MIME 메시지 도우미 객체
     * @param sender 발신자 정보가 담긴 MailAddress 객체
     * @throws MessagingException 메시지 설정 중 발생할 수 있는 예외
     * @throws UnsupportedEncodingException 지원되지 않는 인코딩이 발견될 경우 발생하는 예외
     */
    @Override
    public void setSender(final MimeMessageHelper helper, EmailAddress sender) throws MessagingException, UnsupportedEncodingException {
        if (sender == null) sender = Constant.SYSTEM_EMAIL_ADDRESS;
        helper.setFrom(sender.getInternetAddress());
    }

    /**
     * 수신자를 설정한다.
     *
     * @param helper MIME 메시지 도우미 객체
     * @param recipientList 수신자 정보가 담긴 MailAddress 객체의 리스트
     * @throws MessagingException 수신자 설정 중 발생할 수 있는 예외
     */
    @Override
    public void setRecipients(MimeMessageHelper helper, List<EmailAddress> recipientList) throws MessagingException, UnsupportedEncodingException {
        final List<InternetAddress> list = new ArrayList<>();
        for (final EmailAddress mailAddress : recipientList) {
            InternetAddress internetAddress = mailAddress.getInternetAddress();
            list.add(internetAddress);
        }
        helper.setTo(list.toArray(new InternetAddress[0]));
    }
}
