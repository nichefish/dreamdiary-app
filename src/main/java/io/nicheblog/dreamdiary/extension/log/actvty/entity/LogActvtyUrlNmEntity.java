package io.nicheblog.dreamdiary.extension.log.actvty.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * LogActvtyUrlNmEntity
 * <pre>
 *  로그 작업 URL 한글 매칭 Entity.
 * </pre>
 *
 *
 * @author nichefish
 */
@Entity
@Table(name = "log_actvty_url_nm")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
public class LogActvtyUrlNmEntity
        implements Serializable {

    /** URL (PK) */
    @Id
    @Column(name = "url", length = 200, updatable = false)
    @Comment("URL (PK)")
    private String url;

    /** URL 이름 */
    @Column(name = "url_nm", length = 2000)
    @Comment("URL 이름")
    private String urlNm;

    /** 삭제 여부 (Y/N) */
    @Builder.Default
    @Column(name = "del_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("삭제 여부")
    private String delYn = "N";
}
