package io.nicheblog.dreamdiary.feature.board.group.type;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 구조적으로 {@code board.board_key} 와 맞물려야 하는 "예약" 게시판 코드 목록.
 * <p>
 * 게시판 관리 화면 사이드바 표는 템플릿에 문자열을 하드코딩하지 않고, 이 enum 만을 순회해 렌더링한다.
 * 신규 도메인이 동일 패턴으로 게시판 행을 참조하면 상수를 추가한다.
 * </p>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum ReservedStructuralBoard {

    BOARD(ContentType.BOARD),

    JOURNAL_DAY(ContentType.JOURNAL_DAY),
    JOURNAL_CHAPTER(ContentType.JOURNAL_CHAPTER),
    JOURNAL_DIARY(ContentType.JOURNAL_DIARY),
    JOURNAL_NOTE(ContentType.JOURNAL_NOTE),
    JOURNAL_DREAM(ContentType.JOURNAL_DREAM),
    JOURNAL_INTERPRETATION(ContentType.JOURNAL_INTERPRETATION),
    JOURNAL_THREAD(ContentType.JOURNAL_THREAD),
    JOURNAL_ANNUAL(ContentType.JOURNAL_ANNUAL),
    JOURNAL_ANNUAL_REVIEW(ContentType.JOURNAL_ANNUAL_REVIEW),
    JOURNAL_TODO(ContentType.JOURNAL_TODO),

    SCHEDULE(ContentType.SCHEDULE),
    CHAT_MESSAGE(ContentType.CHAT_MESSAGE),
    ;

    private final ContentType contentType;

    /**
     * DB {@code board.board_key} 및 각 엔티티 {@code content_type} 과 동일한 코드 값.
     *
     * @return 게시판 키 문자열
     */
    public String getBoardKey() {
        return contentType.getKey();
    }

    /**
     * 사이드바 "사용 엔티티" 열에 표시할 설명.
     * 현재는 {@link ContentType#getDesc()} 에 위임한다.
     *
     * @return 표시명
     */
    public String getConsumerLabel() {
        return contentType.getLabel();
    }

    /**
     * {@code board_key} 가 {@link ContentType} 기반 구조적 예약 코드와 동일한 값인지 판별한다.
     * <p>
     * DB에 행이 없어도 true가 될 수 있으므로, "이미 사용 중" 여부는 별도로
     * {@code board} 테이블 중복 조회로 판단해야 한다.
     * </p>
     *
     * @param boardKey 등록·수정하려는 게시판 키
     * @return 예약 목록에 정의된 키와 일치하면 true
     */
    public static boolean isStructuralReservedKey(final String boardKey) {
        if (boardKey == null || boardKey.isBlank()) {
            return false;
        }
        final String trimmed = boardKey.trim();
        for (final ReservedStructuralBoard r : values()) {
            if (r.getBoardKey().equals(trimmed)) {
                return true;
            }
        }
        return false;
    }
}
