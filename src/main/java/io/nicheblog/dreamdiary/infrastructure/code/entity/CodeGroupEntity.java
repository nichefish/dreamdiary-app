package io.nicheblog.dreamdiary.infrastructure.code.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeGroupEntity
 * <pre>
 *  분류 코드(groupCode) Entity.
 *  ※분류 코드(group_code) = 상위 분류. 하위 code_item을 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "code_group")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Where(clause = "deleted_at IS NULL")
public class CodeGroupEntity
        extends BaseAuditEntity
        implements Usable {

    @PostLoad
    private void onLoad() {
        this.codeItemCnt = (CollectionUtils.isEmpty(this.codeItems)) ? 0 : this.codeItems.size();
    }

    /** 내부 PK (id) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 분류 코드 (비즈니스 키, UNIQUE) */
    @Column(name = "group_code", length = 50, unique = true)
    private String groupCode;

    /** 분류 코드 이름 */
    @Column(name = "group_name")
    private String groupName;

    /** 분류 코드 설명 */
    @Column(name = "description", length = 2000)
    private String description;

    /** 정렬 순서 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "N";

    /** 시스템 보호 여부 (Y/N) */
    @Builder.Default
    @Column(name = "protected_yn")
    @Comment("시스템 보호 여부 (Y/N)")
    private String protectedYn = "N";

    /** 하위 code_item 목록 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_code", referencedColumnName = "group_code", insertable = false, updatable = false)
    @BatchSize(size = 10)
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private List<CodeItemEntity> codeItems;

    /** 하위 코드 개수 */
    @Transient
    @Builder.Default
    private Integer codeItemCnt = 0;

    /* ----- */

    /**
     * 서브엔티티 List 처리를 위한 Setter
     * 한 번 Entity가 생성된 이후부터는 새 List를 할당하면 안 되고 계속 JPA 이력이 추적되어야 한다.
     *
     * @param codeItems 설정할 객체 리스트
     */
    public void setCodeItems(final List<CodeItemEntity> codeItems) {
        if (CollectionUtils.isEmpty(codeItems)) return;
        if (this.codeItems == null) {
            this.codeItems = new ArrayList<>(codeItems);
        } else {
            this.codeItems.clear();
            this.codeItems.addAll(codeItems);
        }
    }
}
