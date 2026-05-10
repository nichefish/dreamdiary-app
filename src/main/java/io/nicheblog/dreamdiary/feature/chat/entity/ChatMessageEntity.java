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
 *  채팅 메세지 Entity (테이블: chat_message).
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

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.CHAT_MESSAGE;

    /** 글 번호 :: 복합키 사용, 시퀀스 생성 로직을 위해 재정의 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("글번호 (key)")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'CHAT_MESSAGE'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 메시지 역할 */
    @Builder.Default
    @Column(name = "role", length = 20)
    @Comment("메시지 역할")
    private String role = "USER";

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    private String content;

    /** 글 분류 코드 */
    @Column(name = "category_code", length = 50)
    @Comment("글 분류 코드")
    private String categoryCode;

    /* ----- */
}
