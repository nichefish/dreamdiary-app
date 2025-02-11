package io.nicheblog.dreamdiary.extension.cache.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RedisConnChecker
 * <pre>
 *  레디스 연결 상태를 체크하고 상태에 따라 캐시 전략 전환 및 기존 캐시 초기화.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class RedisConnChecker {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Redis 연결 상태를 캐시하기 위한 변수
    private boolean isAvailable = false;
    private long lastChecked = 0;
    
    private boolean prevStatus = true;

    private static final long CHECK_INTERVAL = 30000; // 30초마다 Redis 연결 체크

    /**
     * Redis 연결을 확인하는 메소드
     */
    public void checkRedisConnection() {
        long currentTime = System.currentTimeMillis();
        // 일정 시간(예: 30초) 동안 연결 상태를 재확인하지 않음
        if (currentTime - lastChecked > CHECK_INTERVAL) {
            try {
                assert redisTemplate.getConnectionFactory() != null;

                redisTemplate.getConnectionFactory().getConnection().ping();
                isAvailable = true;
                
                boolean becameAvailable = !prevStatus;
                if (becameAvailable) {
                    // 연결 불가 상태로부터 회복시 캐시 일관성 유지 위해 기존 캐시 클리어.
                    log.info("Redis connection restored.");
                    this.clearRedisCache();
                }
            } catch (final Exception e) {
                isAvailable = false;
                final boolean becameUnavailable = prevStatus;
                if (becameUnavailable) {
                    log.error("Redis connection became botched.");
                }
                log.error("Redis connection failed: {}", e.getMessage());
            }
            // 이전 상태와 비교하여 상태 변화 추적
            prevStatus = isAvailable;
            lastChecked = currentTime;  // 마지막 체크 시간을 갱신
        }
    }

    /**
     * Redis 연결 상태를 가져오는 메소드
     *
     * @return Redis 연결 상태
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * 레디스 전체 캐시 클리어.
     */
    private void clearRedisCache() {
        log.info("Clearing Redis cache.");
        assert redisTemplate.getConnectionFactory() != null;

        // 여기에 Redis 캐시 삭제 로직 추가 (예: Redis에서 키를 제거하거나 전체 캐시를 비우는 방법)
        redisTemplate.getConnectionFactory().getConnection().flushAll(); // 예시: 전체 캐시 삭제
    }
}
