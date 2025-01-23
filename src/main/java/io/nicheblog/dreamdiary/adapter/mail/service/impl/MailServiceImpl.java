package io.nicheblog.dreamdiary.adapter.mail.service.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.nicheblog.dreamdiary.adapter.mail.model.MailAddress;
import io.nicheblog.dreamdiary.adapter.mail.model.MailSendParam;
import io.nicheblog.dreamdiary.adapter.mail.service.MailService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.global._common.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.global._common.log.sys.model.LogSysParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

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
public class MailServiceImpl
        implements MailService {

    private final JavaMailSender mailSender;
    private final Configuration freemarkerMailConfiguration;
    private final ApplicationEventPublisher publisher;

    /**
     * 메일 발송
     *
     * @param mailSendParam 메일 발송에 필요한 정보가 담긴 MailSendParam 객체
     * @return {@link Boolean} -- 메일 발송 시도 결과
     * @see LogSysEventListener
     */
    @Override
    public Boolean send(MailSendParam mailSendParam) {
        try {
            // 메세지 생성
            final MimeMessage message = this.createMimeMessage(mailSendParam);
            // 생성된 메세지 발송
            mailSender.send(message);
        } catch (final Exception e) {
            log.warn("mail send failed", e);
            final LogSysParam logParam = new LogSysParam(true, "메일 발송에 실패했습니다.", ActvtyCtgr.SYSTEM);
            logParam.setExceptionInfo(e);
            publisher.publishEvent(new LogSysEvent(this, logParam));
        }
        return true;
    }

    /**
     * MIME 메시지를 생성합니다.
     *
     * @param mailSendParam 메일 발송에 필요한 정보가 담긴 MailSendParam 객체
     * @return 생성된 MimeMessage 객체
     * @throws Exception 메시지 생성 중 발생할 수 있는 예외
     */
    @Override
    public MimeMessage createMimeMessage(MailSendParam mailSendParam) throws Exception {
        final MimeMessage message = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, true, Constant.CHARSET_UTF_8);

        // 발신자 세팅 :: 메소드 분리
        this.setSender(helper, mailSendParam.getSender());
        // 제목
        helper.setSubject(mailSendParam.getSubject());
        // 수신자
        this.setRecipients(helper, mailSendParam.getRecipientList());
        // 내용 :: Freemarker 템플릿 처리
        final Map<String, Object> dataMap = mailSendParam.getDataMap();
        final Template tmplat = freemarkerMailConfiguration.getTemplate(mailSendParam.getTmplat());
        final String compiledContent = FreeMarkerTemplateUtils.processTemplateIntoString(tmplat, dataMap);
        helper.setText(compiledContent, true);
        // 첨부파일 처리
        mailSendParam.getAtchFileList().forEach(attachFile -> {
            try {
                helper.addAttachment(attachFile.getOrgnFileNm(), new File(attachFile.getFileStrePath()));
            } catch (MessagingException e) {
                log.error("Error attaching file", e);
            }
        });

        return message;
    }

    /**
     * 발신자를 설정합니다.
     *
     * @param helper MIME 메시지 도우미 객체
     * @param sender 발신자 정보가 담긴 MailAddress 객체
     * @throws MessagingException 메시지 설정 중 발생할 수 있는 예외
     * @throws UnsupportedEncodingException 지원되지 않는 인코딩이 발견될 경우 발생하는 예외
     */
    @Override
    public void setSender(final MimeMessageHelper helper, MailAddress sender) throws MessagingException, UnsupportedEncodingException {
        final String WEBMASTER_NAME = "webmaster";
        final String WEBMASTER_EMAIL = "webmaster@email.net";
        if (sender == null) sender = new MailAddress(WEBMASTER_EMAIL, WEBMASTER_NAME);
        helper.setFrom(sender.getInternetAddress());
    }

    /**
     * 수신자를 설정합니다.
     *
     * @param helper MIME 메시지 도우미 객체
     * @param recipientList 수신자 정보가 담긴 MailAddress 객체의 리스트
     * @throws MessagingException 수신자 설정 중 발생할 수 있는 예외
     */
    @Override
    public void setRecipients(MimeMessageHelper helper, List<MailAddress> recipientList) throws MessagingException, UnsupportedEncodingException {
        final List<InternetAddress> list = new ArrayList<>();
        for (final MailAddress mailAddress : recipientList) {
            InternetAddress internetAddress = mailAddress.getInternetAddress();
            list.add(internetAddress);
        }
        helper.setTo(list.toArray(new InternetAddress[0]));
    }
}
