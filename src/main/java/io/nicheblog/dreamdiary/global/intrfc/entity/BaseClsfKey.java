package io.nicheblog.dreamdiary.global.intrfc.entity;

import io.nicheblog.dreamdiary.extension.ContentType;
import lombok.*;

import java.io.Serializable;

/**
 * BaseClsfKey
 * <pre>
 *  (공통/상속) 게시판 복합키 Entity. (postNo + contentType)
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
    protected Integer postNo;

    /** 컨텐츠 타입 */
    protected String contentType;

    /* ----- */

    /**
     * 생성자.
     * @param postNo 글 번호
     * @param type 콘텐츠 타입
     */
    public BaseClsfKey(final Integer postNo, final ContentType type) {
        this.postNo = postNo;
        this.contentType = type.key;
    }
}
