package io.nicheblog.dreamdiary.feature.journal;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * 저널 소유권 통합 테스트용 가상 사용자 준비 도구.
 * 테스트 트랜잭션 안에서 username과 영속 ID가 함께 존재하는 인증 주체를 구성한다.
 *
 * @author nichefish
 */
@UtilityClass
public class JournalTestUserSupport {

    /**
     * 가상 사용자를 조회하거나 생성하고 영속 ID를 반환한다.
     *
     * @param userRepository 사용자 저장소
     * @param username 가상 사용자 로그인명
     * @return 사용자 영속 ID
     */
    public static Integer ensureUser(final UserRepository userRepository, final String username) throws Exception {
        final UserEntity existing = userRepository.findByUsername(username).orElse(null);
        if (existing != null) return existing.getId();

        final UserEntity user = UserEntityTestFactory.create();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        return userRepository.saveAndFlush(user).getId();
    }

    /**
     * 지정 가상 사용자를 현재 인증 주체로 설정한다.
     *
     * @param userId 사용자 영속 ID
     * @param username 가상 사용자 로그인명
     */
    public static void authenticate(final Integer userId, final String username) {
        final AuthInfo principal = AuthInfo.builder()
                .userId(userId)
                .username(username)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }
}
