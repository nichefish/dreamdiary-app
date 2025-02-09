package io.nicheblog.dreamdiary.extension.notify.email.model;

import io.nicheblog.dreamdiary.global.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

/**
 * EmailAddress
 * <pre>
 *  메일 (발신자/수신자) 주소
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailAddress {

    /** 이메일 */
    private String email;
    /** 이름 */
    private String name;

    /* ----- */

    /**
     * 사용자의 이메일과 이름을 기반으로 {@link InternetAddress} 객체를 반환합니다.
     *
     * @return {@link InternetAddress} -- 이메일과 이름이 설정된 {@link InternetAddress} 객체
     * @throws UnsupportedEncodingException 이메일 또는 이름의 인코딩 처리 중 발생할 수 있는 예외
     */
    public InternetAddress getInternetAddress(final String email, final String name) throws UnsupportedEncodingException {
        return new InternetAddress(email, name, Constant.CHARSET_UTF_8);
    }

    /**
     * 사용자의 이메일과 이름을 기반으로 {@link InternetAddress} 객체를 반환합니다.
     *
     * @return {@link InternetAddress} -- 이메일과 이름이 설정된 {@link InternetAddress} 객체
     * @throws UnsupportedEncodingException 이메일 또는 이름의 인코딩 처리 중 발생할 수 있는 예외
     */
    public InternetAddress getInternetAddress() throws UnsupportedEncodingException {
        return new InternetAddress(email, name, Constant.CHARSET_UTF_8);
    }
}
