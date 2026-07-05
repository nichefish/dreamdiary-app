package io.nicheblog.dreamdiary.infrastructure.release.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.infrastructure.release.type.ReleaseEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;

import java.time.LocalDateTime;

/**
 * ReleaseHistoryEntity
 * <pre>
 *  서버 시작 및 배포 이력 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "release_info")
@DynamicInsert
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseHistoryEntity
        extends BaseAuditRegEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("릴리즈 히스토리 ID")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    @Comment("이벤트 타입")
    private ReleaseEventType eventType;

    @Column(name = "app_version", nullable = false, length = 50)
    @Comment("애플리케이션 버전")
    private String appVersion;

    @Column(name = "commit_hash", nullable = false, length = 100)
    @Comment("커밋 해시")
    private String commitHash;

    @Column(name = "release_key", nullable = false, length = 160)
    @Comment("릴리즈 식별 키")
    private String releaseKey;

    @Column(name = "started_at")
    @Comment("서버 시작 시각")
    private LocalDateTime startedAt;

    @Column(name = "deployed_at")
    @Comment("배포 판정 시각")
    private LocalDateTime deployedAt;

    @Column(name = "profile", length = 20)
    @Comment("실행 프로필")
    private String profile;

    @Column(name = "host_name", length = 255)
    @Comment("호스트 이름")
    private String hostName;

    @Column(name = "instance_id", length = 255)
    @Comment("인스턴스 식별자")
    private String instanceId;
}
