package io.nicheblog.dreamdiary.infrastructure.release.model;

import io.nicheblog.dreamdiary.infrastructure.release.entity.ReleaseHistoryEntity;
import io.nicheblog.dreamdiary.infrastructure.release.type.ReleaseEventType;
import lombok.Builder;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * ReleaseHistoryDto
 * <pre>
 *  release_info 응답 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Builder
public class ReleaseHistoryDto {
    private final Integer id;
    private final ReleaseEventType eventType;
    private final String appVersion;
    private final String commitHash;
    private final String releaseKey;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime startedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime deployedAt;
    private final String profile;
    private final String hostName;
    private final String instanceId;
    private final String createdBy;
    /** 등록일시 — LocalDateTime 전환 후에도 API 직렬화 포맷(yyyy-MM-dd HH:mm:ss) 계약 유지 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    public static ReleaseHistoryDto fromEntity(final ReleaseHistoryEntity entity) {
        return ReleaseHistoryDto.builder()
                .id(entity.getId())
                .eventType(entity.getEventType())
                .appVersion(entity.getAppVersion())
                .commitHash(entity.getCommitHash())
                .releaseKey(entity.getReleaseKey())
                .startedAt(entity.getStartedAt())
                .deployedAt(entity.getDeployedAt())
                .profile(entity.getProfile())
                .hostName(entity.getHostName())
                .instanceId(entity.getInstanceId())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
