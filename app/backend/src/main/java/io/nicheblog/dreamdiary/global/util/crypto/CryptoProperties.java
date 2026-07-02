package io.nicheblog.dreamdiary.global.util.crypto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CryptoProperties
 * <pre>
 *  Cryptography settings.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "app.crypto")
@Getter
@Setter
public class CryptoProperties {

    /** AES-128 settings. */
    private Aes128 aes128 = new Aes128();

    /**
     * AES-128 settings.
     */
    @Getter
    @Setter
    public static class Aes128 {
        /** AES-128 secret key. */
        private String secretKey = "0000000000000000";
    }
}
