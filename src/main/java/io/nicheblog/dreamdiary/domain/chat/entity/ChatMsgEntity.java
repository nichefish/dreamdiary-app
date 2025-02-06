package io.nicheblog.dreamdiary.domain.chat.entity;

import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.global.intrfc.entity.BasePostEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * ChatMsgEntity
 * <pre>
 *  채팅 메세지 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "chat_msg")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE chat_msg SET del_yn = 'Y' WHERE post_no = ?")
public class ChatMsgEntity
        extends BasePostEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.CHAT_MSG;

    /** 글 번호 :: 복합키 사용, 시퀀스 생성 로직을 위해 재정의 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("글번호 (key)")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'CHAT_MSG'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;
}
