package io.nicheblog.dreamdiary.infrastructure.release.model;

import lombok.Builder;
import lombok.Getter;

/**
 * ReleaseRuntimeMeta
 * <pre>
 *  현재 실행 중인 릴리즈 메타 정보.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Builder
public class ReleaseRuntimeMeta {
    private final String appVersion;
    private final String commitHash;
    private final String releaseKey;
    private final String profile;
    private final String hostName;
    private final String instanceId;
}
