package io.nicheblog.dreamdiary.feature.attachable.prefix.type;

/**
 * Prefix 평면 목록의 관리 소유 유형.
 * <p>
 * {@link #PERSONAL}은 특정 사용자의 content type별 목록이고,
 * {@link #GLOBAL}은 사용자 개인 소유가 아닌 content type별 공용 관리 목록이다.
 * GLOBAL은 모든 콘텐츠가 목록 하나를 공유한다는 뜻이 아니며, 게시판에서는
 * {@code board.board_key}마다 독립적인 GLOBAL Scope를 가진다.
 * </p>
 *
 * @author nichefish
 */
public enum PrefixScopeType {

    /** 사용자 개인 소유 목록 */
    PERSONAL,

    /** 애플리케이션·관리자 소유 공용 목록 */
    GLOBAL
}
