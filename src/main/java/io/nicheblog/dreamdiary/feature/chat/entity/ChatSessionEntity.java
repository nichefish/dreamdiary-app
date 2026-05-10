package io.nicheblog.dreamdiary.feature.chat.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * ChatSessionEntity
 * <pre>
 *  AI 채팅 세션 Entity (테이블: chat_session).
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "chat_session")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE chat_session SET deleted_at = NOW() WHERE id = ?")
public class ChatSessionEntity
        extends BaseAuditEntity {

    /** 세션 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("세션 ID")
    private Integer id;

    /** 제목 */
    @Column(name = "title", length = 200)
    @Comment("제목")
    private String title;

    /** 상태 */
    @Builder.Default
    @Column(name = "status", length = 20)
    @Comment("상태")
    private String status = "ACTIVE";

    /** AI 모델 */
    @Column(name = "model", length = 100)
    @Comment("AI 모델")
    private String model;

    /** 시스템 프롬프트 */
    @Column(name = "system_prompt", columnDefinition = "LONGTEXT")
    @Comment("시스템 프롬프트")
    private String systemPrompt;

    /** 마지막 메시지 일시 */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_message_at")
    @Comment("마지막 메시지 일시")
    private Date lastMessageAt;
}
