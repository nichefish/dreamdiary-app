package io.nicheblog.dreamdiary.auth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;


/**
 * RefreshToken
 * <pre>
 *   Redis에서 사용하는 RefreshToken Entity.
 *   TODO
 * </pre>
 *
 * @author nichefish
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@RedisHash(value = "refresh_token")
public class RefreshToken {

    /**
     * RefreshToken ID
     */
    @Id
    private String authId;

    /**
     * redis에 index된 키
     */
    @Indexed
    private String token;

    /**
     * role
     */
    private String role;

    /**
     * 만료시간
     */
    @TimeToLive
    private long ttl;

    public RefreshToken update(final String token, final long ttl) {
        this.token = token;
        this.ttl = ttl;
        return this;
    }

}