package io.nicheblog.dreamdiary.auth.security.service.impl;

import io.nicheblog.dreamdiary.auth.security.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * VerificationCodeService
 * <pre>
 *  인증 코드 관리 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service("verificationCodeService")
@RequiredArgsConstructor
public class VerificationCodeServiceImpl
        implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private static final long EXPIRATION_TIME = 10 * 60; // 10분 만료

    @Override
    public void setVerificationCode(final String email, final String code) {
        redisTemplate.opsForValue().set(getRedisKey(email), code, EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    @Override
    public String getVerificationCode(final String email) {
        return redisTemplate.opsForValue().get(getRedisKey(email));
    }

    @Override
    public void deleteVerificationCode(final String email) {
        redisTemplate.delete(getRedisKey(email));
    }

    @Override
    public String getRedisKey(final String email) {
        return "email_verification:" + email;
    }
}
