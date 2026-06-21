package io.nicheblog.dreamdiary.global.util.crypto;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * CryptoUtils
 * <pre>
 *  암호화 관련 유틸리티 모듈
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@Log4j2
public class CryptoUtils {

    /**
     * AES128 관련 유틸리티 메소드 위임
     */
    public static class AES128 {

        /**
         * AES 암호화(인코딩)를 {@link AES128Module}에 위임한다.
         */
        public static String encrypt(final String data) {
            return AES128Module.encrypt(data);
        }

        /**
         * AES 복호화(디코딩)를 {@link AES128Module}에 위임한다.
         */
        public static String decrypt(final String data) {
            return AES128Module.decrypt(data);
        }
    }

    /** 마스킹 관련 유틸리티 메소드 위임 */
    public static class Mask extends MaskingModule {}
}
