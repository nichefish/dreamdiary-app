package io.nicheblog.dreamdiary.feature.journal.setting.entity;

import io.nicheblog.dreamdiary.feature.journal.setting.type.JournalDefaultEntryView;
import lombok.*;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * JournalSettingEntity
 * <pre>
 *  저널 도메인 설정 Entity.
 *  ADMIN/GLOBAL 행은 전역 정책을, USER/username 행은 사용자별 정책을 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(
        name = "journal_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_journal_setting_scope",
                columnNames = {"scope", "scope_key"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 설정 ID")
    private Integer id;

    @Column(name = "scope", nullable = false, length = 20)
    @Comment("설정 범위 (ADMIN/USER)")
    private String scope;

    @Column(name = "scope_key", nullable = false, length = 100)
    @Comment("범위 키 (ADMIN=GLOBAL, USER=username)")
    private String scopeKey;

    /** AI 임베딩 활성화 여부. true=등록/수정 시 embedding+entity queue 적재, false=건너뜀. */
    @Builder.Default
    @Column(name = "embedding_enabled", nullable = false)
    @Comment("AI 임베딩 활성화 여부 (1=ON, 0=OFF)")
    private Boolean embeddingEnabled = true;

    /** 사용자별 저널 기본 진입 화면. USER 범위에서 사용하며 미설정 값은 서비스 기본값으로 해석한다. */
    @Enumerated(EnumType.STRING)
    @Column(name = "default_entry_view", length = 20)
    @Comment("사용자별 저널 기본 진입 화면 (DAILY/WEEKLY/MONTHLY)")
    private JournalDefaultEntryView defaultEntryView;

    @Column(name = "created_by", length = 20)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 20)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
