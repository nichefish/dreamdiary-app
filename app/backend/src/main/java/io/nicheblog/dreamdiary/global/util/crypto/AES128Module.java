package io.nicheblog.dreamdiary.global.util.crypto;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * AES128Utils
 * <pre>
 *  AES128을 이용한 양방향 암호화 유틸리티 모듈
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
class AES128Module {

    // 비밀키 선언 16바이트
    @Value("${aes128.secret-key:0000000000000000}")
    private static String secretKey;

    /**
     * AES 암호화(인코딩)
     */
    public static String encrypt(final String data) {
        try {
            // AES128비트 암호화에서 16바이트는 변할 수 없다
            byte[] ivBytes = new byte[16];
            // 배열에 초기값 0으로 삽입 실시
            Arrays.fill(ivBytes, (byte) 0x00);

            byte[] textBytes = data.getBytes(StandardCharsets.UTF_8);

            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

            // base64로 다시 포맷해서 인코딩 실시 (경우에 따라 아파치 base64 필요)
            Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(cipher.doFinal(textBytes));
        } catch (final Exception e) {
            log.warn(e.getMessage());
        }
        return "";
    }

    /**
     * AES 복호화(디코딩)
     */
    public static String decrypt(final String data) {
        try {
            // AES128비트 암호화에서 16바이트는 변할 수 없다
            byte[] ivBytes = new byte[16];
            // 배열에 초기값 0으로 삽입 실시
            Arrays.fill(ivBytes, (byte) 0x00);

            // base64로 다시 포맷해서 디코딩 실시 (경우에 따라 아파치 base64 필요)
            Decoder decoder = Base64.getDecoder();
            byte[] textBytes = decoder.decode(data);

            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);

            return new String(cipher.doFinal(textBytes), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            log.warn(e.getMessage());
        }
        return "";
    }
}