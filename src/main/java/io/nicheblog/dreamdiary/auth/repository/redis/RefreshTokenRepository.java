package io.nicheblog.dreamdiary.auth.repository.redis;

import io.nicheblog.dreamdiary.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RefreshTokenRepository
 * <pre>
 *  RefreshToken redis repository 인터페이스
 * </pre>
z *
 * @author nichefish
 */
@Repository("refreshTokenRepository")
public interface RefreshTokenRepository
        extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(final String token);

    Optional<RefreshToken> findByAuthId(final String authId);
}

