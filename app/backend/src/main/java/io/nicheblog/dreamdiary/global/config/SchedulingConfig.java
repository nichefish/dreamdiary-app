package io.nicheblog.dreamdiary.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 테스트 이외 프로필의 정기 작업 실행 설정.
 *
 * <p>테스트 프로필은 영속 데이터베이스를 공유하므로 백그라운드 작업을 실행하지 않는다.</p>
 */
@Configuration
@Profile("!test")
@EnableScheduling
public class SchedulingConfig {
}
