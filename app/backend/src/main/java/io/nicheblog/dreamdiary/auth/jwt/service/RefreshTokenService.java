package io.nicheblog.dreamdiary.auth.jwt.service;

import io.nicheblog.dreamdiary.auth.config.AuthProperties;
import io.nicheblog.dreamdiary.auth.security.exception.AuthenticationFailureException;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * RefreshTokenService
 * <pre>
 *  DB 기반 리프레시 토큰 발급/검증/회전 처리.
 *  - 토큰 자체는 서버에 저장하지 않고 "해시"만 저장 (탈취 대비)
 *  - refresh token은 username + random 조합으로 구성
 *  - rotate 시 기존 토큰 검증 후 "무조건 재발급" (one-time token 구조)
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int RANDOM_BYTES = 32;
    private static final String DELIMITER = ".";
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final UserRepository userRepository;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public long getRefreshTokenTtlSeconds() {
        return authProperties.getRefreshToken().getTtlSeconds();
    }

    @Getter
    @RequiredArgsConstructor
    public static class RefreshResult {
        private final String username;
        private final String refreshToken;
    }

    /**
     * 신규 refresh token 발급
     * - 기존 토큰 여부 상관없이 항상 새 토큰 발급
     *
     * @param username 사용자 계정명
     * @return 리프레시 토큰 문자열
     */
    @Transactional
    public String issue(final String username) {
        if (StringUtils.isBlank(username)) throw new IllegalArgumentException("username is required.");
        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationFailureException());
        return issueForUser(user);
    }

    /**
     * refresh token rotation (핵심 보안 로직)
     * - refresh token은 1회용 → replay 공격 방지
     * - mismatch 시 즉시 revoke → 탈취 대응
     *
     * @param refreshToken 리프레시 토큰 문자열
     * @return 리프레시 토큰
     */
    @Transactional
    public RefreshResult rotate(final String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new AuthenticationFailureException();
        }

        final String username = extractUsername(refreshToken);
        final UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationFailureException());

        if (user.getRefreshTokenHash() == null || user.getRefreshTokenExpiresAt() == null) {
            revoke(user);
            throw new AuthenticationFailureException();
        }

        final LocalDateTime now = DateUtils.getCurrLocalDateTime();
        if (user.getRefreshTokenExpiresAt().isBefore(now)) {
            revoke(user);
            throw new AuthenticationFailureException();
        }

        final String hashed = hashToken(refreshToken);
        if (!secureEquals(hashed, user.getRefreshTokenHash())) {
            revoke(user);
            throw new AuthenticationFailureException();
        }

        final String newToken = issueForUser(user);
        return new RefreshResult(username, newToken);
    }

    /**
     * 사용자 ID 기반 revoke
     * @param username 사용자 계정명
     */
    @Transactional
    public void revoke(final String username) {
        if (StringUtils.isBlank(username)) return;
        userRepository.findByUsername(username).ifPresent(this::revoke);
    }

    /**
     * 사용자 엔티티 기반 revoke
     */
    @Transactional
    public void revoke(final UserEntity user) {
        user.setRefreshTokenHash(null);
        user.setRefreshTokenIssuedAt(null);
        user.setRefreshTokenExpiresAt(null);
        userRepository.save(user);
    }

    /**
     * 실제 토큰 발급
     * @param user 사용자 정보
     * @return 리프레시 토큰 문자열
     */
    private String issueForUser(final UserEntity user) {
        final String refreshToken = generateToken(user.getUsername());
        final String refreshTokenHash = hashToken(refreshToken);
        final LocalDateTime now = DateUtils.getCurrLocalDateTime();

        user.setRefreshTokenHash(refreshTokenHash);
        user.setRefreshTokenIssuedAt(now);
        user.setRefreshTokenExpiresAt(now.plusSeconds(this.getRefreshTokenTtlSeconds()));
        userRepository.saveAndFlush(user);

        return refreshToken;
    }

    /**
     * 리프레시 토큰용 문자열 생성
     *
     * @param username 사용자 계정명
     * @return 생성된 토큰 문자열
     */
    private String generateToken(final String username) {
        final byte[] randomBytes = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        final String userPart = BASE64_URL.encodeToString(username.getBytes(StandardCharsets.UTF_8));
        final String randomPart = BASE64_URL.encodeToString(randomBytes);
        return userPart + DELIMITER + randomPart;
    }

    /**
     * 리프레시 토큰에서 사용자 계정명 추출
     * @param refreshToken 리프레시 토큰 문자열
     * @return 사용자 계정명
     */
    private String extractUsername(final String refreshToken) {
        final int idx = refreshToken.indexOf(DELIMITER);
        if (idx <= 0) throw new AuthenticationFailureException();

        final String encodedUser = refreshToken.substring(0, idx);
        try {
            final byte[] decoded = BASE64_URL_DECODER.decode(encodedUser);
            final String username = new String(decoded, StandardCharsets.UTF_8);
            if (StringUtils.isBlank(username)) {
                throw new AuthenticationFailureException();
            }
            return username;
        } catch (final IllegalArgumentException e) {
            throw new AuthenticationFailureException();
        }
    }

    /**
     * 토큰 해시 (SHA-256)
     *
     * @param token 토큰 문자열
     * @return 해시화된 토큰 문자열
     */
    private String hashToken(final String token) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (final byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to hash refresh token.", e);
        }
    }

    /**
     * constant-time 비교
     * - timing attack 방지. (일반 equals는 문자열 길이/위치에 따라 비교시간 달라짐)
     *
     * @param left String
     * @param right String
     */
    private boolean secureEquals(final String left, final String right) {
        if (left == null || right == null) return false;
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
