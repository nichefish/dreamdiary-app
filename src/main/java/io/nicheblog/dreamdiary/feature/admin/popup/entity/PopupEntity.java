package io.nicheblog.dreamdiary.feature.admin.popup.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * PopupEntity
 * <pre>
 *  팝업 정보 관리 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "POPUP")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE popup SET deleted_at = NOW() WHERE popup_cd = ?")
public class PopupEntity
        extends BaseClsfEntity
        implements FileEmbedModule {

    /** 팝업 코드 */
    @Id
    @Column(name = "popup_cd")
    @Comment("팝업 코드")
    private String popupCd;

    /** 팝업 이름  */
    @Column(name = "popup_nm")
    @Comment("팝업 이름")
    private String popupNm;

    /** 가로 */
    @Column(name = "width")
    @Comment("가로")
    private Integer width;

    /** 세로 */
    @Column(name = "height")
    @Comment("세로")
    private Integer height;

    /** 게시시작일시 */
    @Column(name = "popup_start_dt")
    @Comment("게시시작일시")
    private Date popupStartDt;

    /** 게시종료일시 */
    @Column(name = "popup_end_dt")
    @Comment("게시종료일시")
    private Date popupEndDt;

    /* ----- */

    /** 위임 :: 첨부파일 모듈 */
    @Embedded
    public FileEmbed file;
}
