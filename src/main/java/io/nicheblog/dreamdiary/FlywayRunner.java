package io.nicheblog.dreamdiary;

import io.nicheblog.dreamdiary.global.config.FlywayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * FlywayRunner
 * <pre>
 *  어플리케이션 초기화 로직 수행 클래스.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class FlywayRunner
        implements CommandLineRunner {

    private final DataSource dataSource;
    private final FlywayProperties flywayProperties;

    /**
     * 프로그램 최초 구동시 수행할 로직.
     *
     * @param args 명령줄에서 전달된 인수 목록
     */
    @Override
    public void run(final String... args) throws Exception {

        final String vendor = "mariadb";        // TODO: 추후에 동적 구성?
        final String version = flywayProperties.getTarget();             // 예: 0.14.2
        final String versionPrefix = extractVersionPrefix(version);      // → 0.14.x
        final String location = String.format("classpath:schema/migration/%s/%s", vendor, versionPrefix);
        log.info("FlywayRunner run... location: {}", location);

        final Flyway flyway = Flyway.configure()
                .dataSource(flywayProperties.getUrl(), flywayProperties.getUser(), flywayProperties.getPassword())
                .schemas(flywayProperties.getSchemas().toArray(new String[0]))
                .locations(location)
                .baselineVersion(flywayProperties.getBaselineVersion())
                .target(flywayProperties.getTarget())
                .baselineOnMigrate(flywayProperties.isBaselineOnMigrate())
                .validateOnMigrate(flywayProperties.isValidateOnMigrate())
                .load();
        flyway.migrate();
    }

    private String extractVersionPrefix(final String targetVersion) {
        final int lastDot = targetVersion.lastIndexOf('.');
        return lastDot != -1 ? targetVersion.substring(0, lastDot) + ".x" : targetVersion;
    }
}
