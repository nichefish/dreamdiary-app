package io.nicheblog.dreamdiary.feature.chat.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * ChatMessageEntity
 * <pre>
 *  채팅 메시지 Entity (테이블: chat_message).
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "chat_message")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE chat_message SET deleted_at = NOW() WHERE id = ?")
public class ChatMessageEntity
        extends BaseAttachableEntity {

    /** 첨부/이력 등 공통 기능 연결에 사용하는 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.CHAT_MESSAGE;

    /** 채팅 메시지 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("채팅 메시지 ID")
    private Integer id;

    /** 컨텐츠 타입. 첨부/이력 등 공통 기능 연결용 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'CHAT_MESSAGE'")
    @Comment("컨텐츠 타입. 첨부/이력 등 공통 기능 연결용")
    private String contentType = CONTENT_TYPE.key;

    /** 메시지 역할. USER, ASSISTANT, SYSTEM 등 */
    @Builder.Default
    @Column(name = "role", length = 20)
    @Comment("메시지 역할. USER, ASSISTANT, SYSTEM 등")
    private String role = "USER";

    /** 소속 채팅 세션 ID */
    @Column(name = "session_id")
    @Comment("소속 채팅 세션 ID")
    private Integer sessionId;

    /** 세션 안에서의 메시지 순번 */
    @Column(name = "seq")
    @Comment("세션 안에서의 메시지 순번")
    private Integer seq;

    /** 메시지 제목 또는 표시명 */
    @Column(name = "title")
    @Comment("메시지 제목 또는 표시명")
    private String title;

    /** 메시지 본문 */
    @Column(name = "content")
    @Comment("메시지 본문")
    private String content;

    /** 메시지 분류 코드 */
    @Column(name = "category_code", length = 50)
    @Comment("메시지 분류 코드")
    private String categoryCode;

    /** 메시지 부가 메타데이터 JSON */
    @Column(name = "metadata_json", columnDefinition = "LONGTEXT")
    @Comment("메시지 부가 메타데이터 JSON")
    private String metadataJson;
}
