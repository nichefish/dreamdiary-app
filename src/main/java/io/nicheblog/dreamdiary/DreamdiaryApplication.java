package io.nicheblog.dreamdiary;

import io.github.cdimascio.dotenv.Dotenv;
import io.nicheblog.NicheblogBasePackage;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.YmlLoader;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DreamdiaryApplication
 * <pre>
 *  Spring Boot Application Runner.
 *  (!테스트 의존성 주입 문제로, 메인 클래스는 최대한 간결하게 유지하는 것이 좋다.)
 * </pre>
 *
 * @author nichefish
 * @see DreamdiaryInitializer
 */
@SpringBootApplication(scanBasePackageClasses = { NicheblogBasePackage.class })
@EnableCaching
@EnableScheduling
@Log4j2
public class DreamdiaryApplication {

    /**
     * 애플리케이션의 진입점인 메인(main) 함수입니다.
     *
     * @param args - 명령줄에서 전달된 인수
     */
    public static void main(final String[] args) {
        // 환경 변수는 다른 구동 이전에 세팅되어야 하므로 Application.run() 이전에 로딩되어야 한다.
        loadDotEnvProperties();

        // 시스템 구동
        SpringApplication.run(DreamdiaryApplication.class, args);
    }

    /**
     * 프로필에 따라 dotEnv 설정 로드.
     */
    private static void loadDotEnvProperties() {
        try {
            setDotEnvPropertiesByFileNm("config/env/.env");
            // 프로필 기반 .env.${profile} 프로퍼티 로드 (시점이 맞지 않기 떄문에 .yml 강제 파싱)
            final String profile = YmlLoader.loadSpringProfile();
            if (profile == null) throw new IllegalStateException(MessageUtils.getMessage("common.status.profile-not-set"));

            setDotEnvPropertiesByFileNm("config/env/.env." + profile);
        } catch (final Exception e) {
            log.error("Failed to load .env file for profile '{}'", System.getProperty("spring.profiles.active"), e);
        }
    }

    /**
     * 지정된 프로퍼티 파일을 읽어 시스템 환경 변수에 추가합니다.
     *
     * @param fileName - 로드할 .env 파일의 이름
     */
    private static void setDotEnvPropertiesByFileNm(final String fileName) {
        final Dotenv dotenv = Dotenv.configure().filename(fileName).load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        log.info("Loaded {} file successfully.", fileName);
    }
}
