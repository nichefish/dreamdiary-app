package io.nicheblog.dreamdiary.global;

import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * ActiveProfile
 * <pre>
 *  현재 활성 중인 프로필 정보를 가져온다. (local / dev / stg / prod / test)
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "spring.profiles")
@Getter
@Setter
public class ActiveProfile {

    @PostConstruct
    public void init() throws Exception {
        if (StringUtils.isEmpty(this.active)) throw new IllegalStateException(MessageUtils.getMessage("common.status.profile-invalid"));
        this.profile = Profile.valueOf(this.active.toUpperCase());
    }

    /**
     * 프로필 Enum
     */
    @RequiredArgsConstructor
    public enum Profile {
        LOCAL("local", "로컬 환경"),
        DEV("dev", "개발 환경"),
        STG("stg", "스테이징 환경"),
        PROD("prod", "운영 환경"),
        TEST("test", "테스트 환경");

        public final String key;
        public final String desc;
    }

    private Profile profile;

    public final static String PROFILE_LOCAL = Profile.LOCAL.key;
    public final static String PROFILE_DEV = Profile.DEV.key;
    public final static String PROFILE_STG = Profile.STG.key;
    public final static String PROFILE_PROD = Profile.PROD.key;
    public final static String PROFILE_TEST = Profile.TEST.key;

    /**
     * 활성화된 프로필 항목
     */
    private String active;

    /* ----- */

    public Boolean isLocal() {
        return PROFILE_LOCAL.equals(this.active);
    }

    public Boolean isDev() {
        return PROFILE_DEV.equals(this.active);
    }

    public Boolean isStg() {
        return PROFILE_STG.equals(this.active);
    }

    public Boolean isProd() {
        return PROFILE_PROD.equals(this.active);
    }

    public Boolean isTest() {
        return PROFILE_TEST.equals(this.active);
    }
}
