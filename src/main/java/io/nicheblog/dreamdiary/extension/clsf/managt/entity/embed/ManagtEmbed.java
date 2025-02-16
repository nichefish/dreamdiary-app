package io.nicheblog.dreamdiary.extension.clsf.managt.entity.embed;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.security.util.AuditorUtils;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.clsf.managt.entity.ManagtrEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.OrderBy;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * CommentEmbed
 * <pre>
 *  위임:: 댓글 관련 정보. (entity level)
 * </pre>
 *
 * @author nichefish
 * @see ManagtEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ManagtEmbed
        implements Serializable {

    @PostLoad
    private void onLoad() {
        this.isManagtr = AuthUtils.isRegstr(this.managtrId);
    }

    /** 조치자(작업자)ID */
    @Column(name = "managtr_id", length = 20)
    private String managtrId;

    /** 조치자(작업자) 정보 :: join 제거하고 캐시 처리 */
    @Transient
    private AuditorInfo managtrInfo;

    @Transient
    private String managtrNm;

    /** 나(=조치자) 여부 */
    @Transient
    private Boolean isManagtr;

    /** 조치(작업)일시 */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "managt_dt")
    private Date managtDt;

    /** 게시물 조치자 목록 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no", insertable = false, updatable = false)),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_content_type", referencedColumnName = "content_type", insertable = false, updatable = false))
    })
    @Fetch(FetchMode.SELECT)
    @OrderBy("regDt DESC")
    @NotFound(action = NotFoundAction.IGNORE)
    private List<ManagtrEntity> list;

    /* ----- */

    /** 생성자 */
    public ManagtEmbed() {
        this.managtrId = AuthUtils.getLgnUserId();
    }
    public ManagtEmbed(Boolean updtManagtDt) {
        this();
        if (updtManagtDt) this.managtDt = DateUtils.getCurrDate();
    }

    /**
     * 조치자 정보 반환 :: 캐시 처리
     *
     * @return {@link AuditorInfo}
     */
    public AuditorInfo getManagtrInfo() {
        if (StringUtils.isEmpty(this.managtrId)) return null;
        if (this.managtrInfo == null) {
            this.managtrInfo = AuditorUtils.getAuditorInfo(this.managtrId);
        }
        return this.managtrInfo;
    }

}