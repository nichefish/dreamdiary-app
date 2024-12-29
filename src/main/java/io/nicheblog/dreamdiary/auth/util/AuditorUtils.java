package io.nicheblog.dreamdiary.auth.util;

import io.nicheblog.dreamdiary.auth.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * AuditorUtils
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class AuditorUtils {

    private final AuthService authwiredAuthService;

    private static AuthService authService;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        authService = authwiredAuthService;
    }

    /**
     * getAuditorInfo
     *
     * @param userId 사용자 ID
     * @return AuditorInfo
     */
    public static AuditorInfo getAuditorInfo(String userId) {
        return authService.getAuditorInfo(userId);
    };
}
