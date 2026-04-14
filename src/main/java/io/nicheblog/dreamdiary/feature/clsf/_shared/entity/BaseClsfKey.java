package io.nicheblog.dreamdiary.feature.clsf._shared.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.*;

import java.io.Serializable;

/**
 * BaseClsfKey
 * <pre>
 *  (공통/상속) 게시판 복합키 Entity. (id + contentType)
 * </pre>
 *
 * @author nichefish
 * @see BaseClsfEntity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BaseClsfKey
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
    public BaseClsfKey(final Integer id, final ContentType type) {
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
