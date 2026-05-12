package io.nicheblog.dreamdiary.infrastructure.release.model;

import io.nicheblog.dreamdiary.infrastructure.release.entity.ReleaseHistoryEntity;
import io.nicheblog.dreamdiary.infrastructure.release.type.ReleaseEventType;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

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
    private final Date startedAt;
    private final Date deployedAt;
    private final String profile;
    private final String hostName;
    private final String instanceId;
    private final String createdBy;
    private final Date createdAt;

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
