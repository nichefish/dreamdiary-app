package io.nicheblog.dreamdiary.infrastructure.log.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 로그 작업 URL 한글 매칭.
 */
@Entity
@Table(name = "log_url_nm")
@DynamicInsert
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class LogUrlNmEntity
        implements Serializable {

    @Id
    @Column(name = "url", length = 200, updatable = false)
    @Comment("URL (PK)")
    private String url;

    @Column(name = "url_nm", length = 2000)
    @Comment("URL 이름")
    private String urlNm;

    @Column(name = "deleted_at")
    @Comment("삭제 일시")
    private Date deletedAt;
}
