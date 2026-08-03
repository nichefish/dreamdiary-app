package io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixScopeEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.type.PrefixScopeType;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 말머리 목록 Scope 저장소.
 *
 * @author nichefish
 */
@Repository
public interface PrefixScopeRepository extends BaseStreamRepository<PrefixScopeEntity, Integer> {

    /**
     * 로그인 사용자의 특정 content_type 개인 Prefix Scope를 조회한다.
     * <p>
     * 목록 정체성은 {@code (PERSONAL, user_id, content_type)}로 정규화되므로,
     * 소유자(username→user.id), 소유 유형, content_type이 모두 일치하는 Scope를 반환한다.
     * 해당 content_type 말머리를 아직 하나도 만들지 않은 사용자는 Scope 행이 없으므로
     * 빈 결과를 반환한다.
     * </p>
     *
     * @param username 로그인 계정명
     * @param contentType 목록 적용 대상 콘텐츠 타입
     * @return 개인 Prefix Scope
     */
    @Query("SELECT s FROM PrefixScopeEntity s, UserEntity u"
            + " WHERE u.username = :username AND s.userId = u.id"
            + " AND s.scopeType = io.nicheblog.dreamdiary.feature.attachable.prefix.type.PrefixScopeType.PERSONAL"
            + " AND s.contentType = :contentType")
    Optional<PrefixScopeEntity> findPersonalScope(@Param("username") String username,
                                                  @Param("contentType") String contentType);

    /**
     * 사용자 개인 소유가 아닌 특정 content_type의 GLOBAL Prefix Scope를 조회한다.
     * 게시판은 {@code contentType=board.board_key}를 사용하며 첫 Prefix 등록 전에는
     * Scope가 없으므로 빈 결과를 반환한다.
     *
     * @param scopeType GLOBAL 고정 소유 유형
     * @param contentType 목록 적용 대상 논리 콘텐츠 타입
     * @return GLOBAL Prefix Scope
     */
    Optional<PrefixScopeEntity> findByScopeTypeAndContentType(PrefixScopeType scopeType, String contentType);

    /**
     * 계정명으로 사용자 PK를 조회한다. 개인 Scope lazy 생성 시 {@code prefix_scope.user_id}를
     * 채우기 위한 최소 조회다.
     *
     * @param username 로그인 계정명
     * @return 사용자 PK
     */
    @Query("SELECT u.id FROM UserEntity u WHERE u.username = :username")
    Optional<Integer> findUserIdByUsername(@Param("username") String username);
}
