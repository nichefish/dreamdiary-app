package io.nicheblog.dreamdiary.feature.chat.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

import java.time.LocalDateTime;

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

    /** 채팅 세션 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("채팅 세션 ID")
    private Integer id;

    /** 세션 제목. 사용자가 수정하거나 첫 메시지 기준으로 생성된다. */
    @Column(name = "title", length = 200)
    @Comment("세션 제목. 사용자가 수정하거나 첫 메시지 기준으로 생성된다.")
    private String title;

    /** 세션 상태. ACTIVE, ARCHIVED 등으로 확장 가능하다. */
    @Builder.Default
    @Column(name = "status", length = 20)
    @Comment("세션 상태. ACTIVE, ARCHIVED 등으로 확장 가능")
    private String status = "ACTIVE";

    /** 세션에서 사용할 AI 모델명. 미지정 시 시스템 기본 모델을 사용한다. */
    @Column(name = "model", length = 100)
    @Comment("세션에서 사용할 AI 모델명. 미지정 시 시스템 기본 모델 사용")
    private String model;

    /** 세션별 시스템 프롬프트. 비어 있으면 기본 시스템 프롬프트를 사용한다. */
    @Column(name = "system_prompt", columnDefinition = "LONGTEXT")
    @Comment("세션별 시스템 프롬프트. 비어 있으면 기본 시스템 프롬프트 사용")
    private String systemPrompt;

    /** 마지막 메시지 작성 일시. 세션 목록 정렬 기준이다. */
    @Column(name = "last_message_at")
    @Comment("마지막 메시지 작성 일시. 세션 목록 정렬 기준")
    private LocalDateTime lastMessageAt;
}
