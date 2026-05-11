package io.nicheblog.dreamdiary.feature.chat.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * ChatSettingEntity
 * <pre>
 *  AI 채팅 설정 Entity (테이블: chat_setting).
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "chat_setting")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE chat_setting SET deleted_at = NOW() WHERE id = ?")
public class ChatSettingEntity
        extends BaseAuditEntity {

    /** 채팅 설정 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("채팅 설정 ID")
    private Integer id;

    /** 설정 범위. USER, ADMIN 등으로 구분한다. */
    @Builder.Default
    @Column(name = "scope", length = 20)
    @Comment("설정 범위. USER, ADMIN 등")
    private String scope = "USER";

    /** 설정 범위 키. USER 범위에서는 사용자 ID를 저장한다. */
    @Column(name = "scope_key", length = 100)
    @Comment("설정 범위 키. USER 범위에서는 사용자 ID")
    private String scopeKey;

    /** AI 응답 생성 시 함께 전달할 최근 대화 메시지 수 */
    @Builder.Default
    @Column(name = "recent_message_limit")
    @Comment("AI 응답 생성 시 함께 전달할 최근 대화 메시지 수")
    private Integer recentMessageLimit = 20;
}
