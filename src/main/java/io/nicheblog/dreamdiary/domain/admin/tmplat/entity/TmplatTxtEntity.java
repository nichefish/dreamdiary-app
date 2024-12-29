package io.nicheblog.dreamdiary.domain.admin.tmplat.entity;

import io.nicheblog.dreamdiary.global._common._clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAtchEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;

/**
 * TmplatTxtEntity
 * <pre>
 *  TEXTAREA (또는 텍스트에디터) 사전입력 템플릿 정의 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "tmplat_txt")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "update tmplat_txt SET del_yn = 'Y' WHERE tmplat_txt_no = ?")
public class TmplatTxtEntity
        extends BaseAtchEntity
        implements StateEmbedModule {

    /** 템플릿 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tmplat_txt_no")
    @Comment("템플릿 번호 (PK)")
    private Integer tmplatTxtNo;

    /** 템플릿 정의 코드 */
    @Column(name = "tmplat_def_cd", length = 1000)
    @Comment("템플릿 정의 코드")
    private String tmplatDefCd;

    /** 템플릿 정의 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tmplat_def_cd", referencedColumnName = "tmplat_def_cd", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("템플릿 정의 정보")
    private TmplatDefEntity tmplatDefInfo;

    /* ---- */

    /** 템플릿 구분 코드 (TEXTAREA vs. TEXTEDITOR) */
    @Column(name = "tmplat_ty_cd")
    @Comment("템플릿 구분 코드")
    private String tmplatTyCd;

    /** 템플릿 코드 */
    @Column(name = "ctgr_cd")
    @Comment("템플릿 코드")
    private String ctgrCd;

    /**
     * 제목
     */
    @Column(name = "title", length = 1000)
    @Comment("제목")
    private String title;

    /**
     * 내용 (텍스트에디터)
     */
    @Column(name = "cn", length = 1000)
    @Comment("내용")
    private String cn;

    /**
     * 기본설정 여부
     */
    @Builder.Default        // Builder 사용시 초기값 세팅하도록 설정
    @Column(name = "default_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("기본설정 여부")
    private String defaultYn = "N";

    /* ----- */

    /** 위임 :: 상태 관리 모듈 */
    @Embedded
    public StateEmbed state;
}
