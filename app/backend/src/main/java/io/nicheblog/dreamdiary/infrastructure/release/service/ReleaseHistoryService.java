package io.nicheblog.dreamdiary.infrastructure.release.service;

import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.infrastructure.release.entity.ReleaseHistoryEntity;
import io.nicheblog.dreamdiary.infrastructure.release.config.ReleaseHistoryProperties;
import io.nicheblog.dreamdiary.infrastructure.release.model.ReleaseHistoryDto;
import io.nicheblog.dreamdiary.infrastructure.release.model.ReleaseRuntimeMeta;
import io.nicheblog.dreamdiary.infrastructure.release.repository.jpa.ReleaseHistoryRepository;
import io.nicheblog.dreamdiary.infrastructure.release.type.ReleaseEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ReleaseHistoryService
 * <pre>
 *  서버 시작/배포 히스토리를 release_info에 기록한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ReleaseHistoryService {

    private final ReleaseHistoryRepository releaseHistoryRepository;
    private final ActiveProfile activeProfile;
    private final Environment environment;
    private final ReleaseHistoryProperties releaseHistoryProperties;

    /**
     * 서버 시작 이벤트를 기록한다.
     */
    public ReleaseHistoryEntity recordServerStart() {
        final ReleaseRuntimeMeta runtimeMeta = resolveRuntimeMeta();
        final ReleaseHistoryEntity saved = releaseHistoryRepository.save(ReleaseHistoryEntity.builder()
                .eventType(ReleaseEventType.SERVER_START)
                .appVersion(runtimeMeta.getAppVersion())
                .commitHash(runtimeMeta.getCommitHash())
                .releaseKey(runtimeMeta.getReleaseKey())
                .startedAt(new Date())
                .profile(runtimeMeta.getProfile())
                .hostName(runtimeMeta.getHostName())
                .instanceId(runtimeMeta.getInstanceId())
                .build());
        log.info("Release history recorded. eventType={} releaseKey={} profile={} instanceId={}",
                saved.getEventType(), saved.getReleaseKey(), saved.getProfile(), saved.getInstanceId());
        return saved;
    }

    /**
     * 배포 이벤트를 조건부 기록한다.
     * 변경 전: 서버 시작과 배포 이벤트의 분리 기록이 없음
     * 변경 후: 직전 DEPLOY 릴리즈 키와 다를 때만 DEPLOY 이벤트를 기록
     */
    public Optional<ReleaseHistoryEntity> recordDeployIfChanged() {
        final ReleaseRuntimeMeta runtimeMeta = resolveRuntimeMeta();
        final Optional<ReleaseHistoryEntity> lastDeployOpt = releaseHistoryRepository.findTopByEventTypeOrderByCreatedAtDesc(ReleaseEventType.DEPLOY);

        if (lastDeployOpt.isPresent() && StringUtils.equals(lastDeployOpt.get().getReleaseKey(), runtimeMeta.getReleaseKey())) {
            log.info("Release history skipped. eventType={} reason=same-release-key releaseKey={}",
                    ReleaseEventType.DEPLOY, runtimeMeta.getReleaseKey());
            return Optional.empty();
        }

        final ReleaseHistoryEntity saved = releaseHistoryRepository.save(ReleaseHistoryEntity.builder()
                .eventType(ReleaseEventType.DEPLOY)
                .appVersion(runtimeMeta.getAppVersion())
                .commitHash(runtimeMeta.getCommitHash())
                .releaseKey(runtimeMeta.getReleaseKey())
                .deployedAt(new Date())
                .profile(runtimeMeta.getProfile())
                .hostName(runtimeMeta.getHostName())
                .instanceId(runtimeMeta.getInstanceId())
                .build());
        log.info("Release history recorded. eventType={} releaseKey={} profile={} instanceId={}",
                saved.getEventType(), saved.getReleaseKey(), saved.getProfile(), saved.getInstanceId());
        return Optional.of(saved);
    }

    /**
     * 최신 DEPLOY 이벤트를 조회한다.
     */
    public Optional<ReleaseHistoryDto> getLatestDeploy() {
        return releaseHistoryRepository.findTopByEventTypeOrderByCreatedAtDesc(ReleaseEventType.DEPLOY)
                .map(ReleaseHistoryDto::fromEntity);
    }

    /**
     * 최신 히스토리 N건을 조회한다.
     *
     * @param size 조회 건수
     * @return 최신순 히스토리
     */
    public List<ReleaseHistoryDto> getRecentHistories(final Integer size) {
        final int safeSize = normalizeListSize(size);
        return releaseHistoryRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeSize))
                .stream()
                .map(ReleaseHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    private int normalizeListSize(final Integer requestedSize) {
        final int fallback = 20;
        if (requestedSize == null || requestedSize <= 0) return fallback;
        final Integer maxListSize = releaseHistoryProperties.getMaxListSize();
        if (maxListSize == null || maxListSize <= 0) return requestedSize;
        return Math.min(requestedSize, maxListSize);
    }

    private ReleaseRuntimeMeta resolveRuntimeMeta() {
        final String appVersion = resolveAppVersion();
        final String commitHash = resolveCommitHash();
        final String releaseKey = appVersion + "+" + commitHash;
        final String profile = StringUtils.defaultIfBlank(activeProfile.getActive(), "unknown");
        final String hostName = resolveHostName();
        final String instanceId = resolveInstanceId(hostName);
        return ReleaseRuntimeMeta.builder()
                .appVersion(appVersion)
                .commitHash(commitHash)
                .releaseKey(releaseKey)
                .profile(profile)
                .hostName(hostName)
                .instanceId(instanceId)
                .build();
    }

    private String resolveAppVersion() {
        final String appVersion = firstNonBlank(
                environment.getProperty("spring.application.version"),
                environment.getProperty("spring.flyway.target"),
                System.getenv("APP_VERSION"),
                System.getProperty("app.version")
        );
        if (StringUtils.isBlank(appVersion)) {
            log.warn("Release meta appVersion is missing. fallback=unknown");
            return "unknown";
        }
        return appVersion;
    }

    private String resolveCommitHash() {
        final String commitHash = firstNonBlank(
                environment.getProperty("git.commit.id.abbrev"),
                environment.getProperty("git.commit.id"),
                System.getenv("GIT_COMMIT"),
                System.getenv("COMMIT_HASH"),
                System.getProperty("git.commit")
        );
        if (StringUtils.isBlank(commitHash)) {
            log.warn("Release meta commitHash is missing. fallback=unknown");
            return "unknown";
        }
        return commitHash;
    }

    private String resolveHostName() {
        try {
            return StringUtils.defaultIfBlank(InetAddress.getLocalHost().getHostName(), "unknown-host");
        } catch (final UnknownHostException e) {
            log.warn("Release meta hostname resolve failed. fallback=unknown-host");
            return "unknown-host";
        }
    }

    private String resolveInstanceId(final String hostName) {
        final String runtimeName = StringUtils.defaultIfBlank(ManagementFactory.getRuntimeMXBean().getName(), "unknown-runtime");
        return hostName + ":" + runtimeName;
    }

    private String firstNonBlank(final String... candidates) {
        for (final String candidate : candidates) {
            if (StringUtils.isNotBlank(candidate)) return candidate;
        }
        return "";
    }
}
