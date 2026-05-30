package io.nicheblog.dreamdiary.feature.attachable._shared.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.*;

import java.io.Serializable;

/**
 * BaseAttachableKey
 * <pre>
 *  (공통/상속) 게시판 복합키 Entity. (id + contentType)
 * </pre>
 *
 * @author nichefish
 * @see BaseAttachableEntity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BaseAttachableKey
        implements Serializable {

    /** 글 번호 */
    protected Integer id;

    /** 컨텐츠 타입 */
    protected String contentType;

    /* ----- */

    /**
     * 생성자.
     * @param id 글 번호
     * @param type 콘텐츠 타입
     */
    public BaseAttachableKey(final Integer id, final ContentType type) {
        this.id = id;
        this.contentType = type.key;
    }

    /**
     * 컨텐츠 타입 enum 객체 반환
     * @return {@link ContentType}
     */
    public ContentType getContentTypeEnum() {
        return ContentType.get(this.contentType);
    }
}
