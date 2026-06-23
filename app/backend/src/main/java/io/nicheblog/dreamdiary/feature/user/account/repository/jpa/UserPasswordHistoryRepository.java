package io.nicheblog.dreamdiary.feature.user.account.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.account.entity.UserPasswordHistoryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserPasswordHistoryRepository
 * <pre>
 *  Password history repository.
 * </pre>
 *
 * @author nichefish
 */
@Repository("userPasswordHistoryRepository")
public interface UserPasswordHistoryRepository
        extends BaseStreamRepository<UserPasswordHistoryEntity, Integer> {

    /**
     * Find password history rows ordered from newest to oldest.
     *
     * @param userId User ID
     * @return ordered password history rows
     */
    List<UserPasswordHistoryEntity> findByUserIdOrderByChangedAtDescIdDesc(final Integer userId);
}
