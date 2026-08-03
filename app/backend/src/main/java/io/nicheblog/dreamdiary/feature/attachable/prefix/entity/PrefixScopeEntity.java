package io.nicheblog.dreamdiary.feature.attachable.prefix.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.type.PrefixScopeType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 평면 말머리 목록의 소유·선택 경계를 나타내는 영속 엔티티.
 * <p>
 * 개인 목록 정체성은 {@code (PERSONAL, user_id, content_type)}, 공용 목록 정체성은
 * {@code (GLOBAL, content_type)}으로 정규화한다. Prefix는 정확히 하나의 Scope에
 * 속하며, 각 관리 문맥에서 첫 Prefix를 등록하는 시점에 lazy 생성한다.
 * </p>
 * <p>
 * 변경 전/후: 이전에는 개인 Scope만 {@code user_id NOT NULL}로 표현하고 게시판은
 * {@code board.prefix_scope_id}로 소유자 없는 Scope를 직접 참조했다. 변경 후에는
 * {@link PrefixScopeType}으로 소유 유형을 명시하고, 게시판도 {@code GLOBAL + boardKey}
 * 정체성으로 Scope를 조회한다.
 * </p>
 *
 * @author nichefish
 */
@Entity
@Table(
        name = "prefix_scope",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_prefix_scope_type_owner_content",
                columnNames = {"scope_type", "owner_key", "content_type"}
        )
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class PrefixScopeEntity extends BaseAuditEntity {

    /** Prefix Scope ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Scope 관리 소유 유형 :: PERSONAL 또는 GLOBAL */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private PrefixScopeType scopeType;

    /** 목록 소유자 :: PERSONAL은 사용자 PK({@code user.id}), GLOBAL은 {@code NULL} */
    @Column(name = "user_id")
    private Integer userId;

    /** 목록 적용 대상 논리 콘텐츠 타입 :: 저널 타입 또는 게시판 {@code boardKey} */
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    /**
     * Scope 유일키용 정규화 소유자 키.
     * MariaDB 생성 컬럼 {@code COALESCE(user_id, 0)}이 채우므로 애플리케이션은 쓰지 않는다.
     */
    @Column(name = "owner_key", insertable = false, updatable = false)
    private Integer ownerKey;
}
