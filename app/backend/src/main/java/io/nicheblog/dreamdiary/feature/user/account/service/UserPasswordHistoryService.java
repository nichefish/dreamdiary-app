package io.nicheblog.dreamdiary.feature.user.account.service;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserPasswordHistoryEntity;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserPasswordHistoryRepository;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserPasswordHistoryService
 * <pre>
 *  Password history validation and pruning service.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserPasswordHistoryService {

    private static final int DEFAULT_PASSWORD_HISTORY_COUNT = 2;

    private final UserPasswordHistoryRepository userPasswordHistoryRepository;
    private final AuthPolicyQueryService authPolicyQueryService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validate that the new password does not match the current password or recent password history.
     *
     * @param user User entity
     * @param newPassword New plain password
     */
    @Transactional(readOnly = true)
    public void validateReusablePassword(final UserEntity user, final String newPassword) throws Exception {
        final int passwordHistoryCount = this.getPasswordHistoryCount();
        if (passwordHistoryCount <= 0) {
            log.debug("Password history validation skipped. userId={}, passwordHistoryCount={}", user == null ? null : user.getId(), passwordHistoryCount);
            return;
        }
        if (user == null || user.getId() == null || StringUtils.isBlank(newPassword)) return;

        if (StringUtils.isNotBlank(user.getPassword()) && passwordEncoder.matches(newPassword, user.getPassword())) {
            log.warn("Password history validation rejected current password reuse. userId={}", user.getId());
            throw new BadCredentialsException(MessageUtils.getMessage("user.pw.history-reused"));
        }

        final List<UserPasswordHistoryEntity> histories = userPasswordHistoryRepository.findByUserIdOrderByChangedAtDescIdDesc(user.getId());
        final boolean reused = histories.stream()
                .limit(passwordHistoryCount)
                .map(UserPasswordHistoryEntity::getPasswordHash)
                .filter(StringUtils::isNotBlank)
                .anyMatch(passwordHash -> passwordEncoder.matches(newPassword, passwordHash));
        if (reused) {
            log.warn("Password history validation rejected recent password reuse. userId={}, passwordHistoryCount={}", user.getId(), passwordHistoryCount);
            throw new BadCredentialsException(MessageUtils.getMessage("user.pw.history-reused"));
        }
    }

    /**
     * Record the previous password hash and prune old rows to the active policy count.
     *
     * @param user User entity
     * @param previousPasswordHash Previous password hash
     */
    @Transactional
    public void recordPasswordChange(final UserEntity user, final String previousPasswordHash) throws Exception {
        if (user == null || user.getId() == null || StringUtils.isBlank(previousPasswordHash)) return;

        final int passwordHistoryCount = this.getPasswordHistoryCount();
        if (passwordHistoryCount <= 0) {
            this.prune(user.getId(), 0);
            log.info("Password history cleared because policy is disabled. userId={}", user.getId());
            return;
        }

        userPasswordHistoryRepository.save(UserPasswordHistoryEntity.builder()
                .userId(user.getId())
                .passwordHash(previousPasswordHash)
                .changedAt(DateUtils.getCurrDate())
                .build());
        this.prune(user.getId(), passwordHistoryCount);
    }

    private int getPasswordHistoryCount() throws Exception {
        final AuthPolicyEntity authPolicy = authPolicyQueryService.getDtlEntity();
        if (authPolicy == null || authPolicy.getPasswordHistoryCount() == null) return DEFAULT_PASSWORD_HISTORY_COUNT;
        return authPolicy.getPasswordHistoryCount();
    }

    private void prune(final Integer userId, final int keepCount) {
        final List<UserPasswordHistoryEntity> histories = userPasswordHistoryRepository.findByUserIdOrderByChangedAtDescIdDesc(userId);
        if (histories.size() <= keepCount) return;

        final List<UserPasswordHistoryEntity> targets = histories.subList(keepCount, histories.size());
        userPasswordHistoryRepository.deleteAll(targets);
        log.info("Password history pruned. userId={}, keepCount={}, deletedCount={}", userId, keepCount, targets.size());
    }
}
