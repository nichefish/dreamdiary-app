package io.nicheblog.dreamdiary.feature.clsf.shared.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * BaseClsfEntity
 * <pre>
 *  (공통/상속) 분류 속성 Entity.
 *  "All classes in the hierarchy must be annotated with @SuperBuilder."
 * </pre>
 *
 * @author nichefish
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BaseClsfEntity
        extends BaseAuditEntity {

    /**
     * 글 번호 (POST_NO, PK)
     * !상속받은 클래스에서 실제 매핑 구성하기 (auto_increment 또는 테이블 생성 전략(for 복합키))
     */
    @Transient
    protected Integer postNo;

    /**
     * 게시판 분류 코드
     * !상속받은 클래스에서 실제 매핑 구성하기 (@Column(name="content_type")
     */
    @Transient
    protected String contentType;

    /* ----- */

    /**
     * 복합키 객체 반환.
     * @return {@link BaseClsfKey} -- 글 번호와 콘텐츠 유형을 포함하는 복합키 객체
     */
    public BaseClsfKey getClsfKey() {
        return new BaseClsfKey(this.postNo, this.contentType);
    }
}
