package io.nicheblog.dreamdiary.feature.attachable._shared.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ContentType
 * <pre>
 *  AttachableEntity를 상속받은 클래스들이 사용하는 컨텐츠 타입 정보
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum ContentType {

    DEFAULT("DEFAULT", "기본"),
    BOARD("BOARD", "일반게시판"),

    JOURNAL_DAY("JOURNAL_DAY", "저널 일자", "calendar3"),
    JOURNAL_CHAPTER("JOURNAL_CHAPTER", "저널 챕터", "book"),
    JOURNAL_DIARY("JOURNAL_DIARY", "저널 일기", "book"),
    JOURNAL_NOTE("JOURNAL_NOTE", "저널 노트", "book"),
    JOURNAL_DREAM("JOURNAL_DREAM", "저널 꿈", "moon-stars"),
    JOURNAL_INTERPRETATION("JOURNAL_INTERPRETATION", "저널 해석", "book"),
    JOURNAL_THREAD("JOURNAL_THREAD", "저널 스레드"),
    JOURNAL_ANNUAL("JOURNAL_ANNUAL", "저널 연간"),
    JOURNAL_ANNUAL_REVIEW("JOURNAL_ANNUAL_REVIEW", "저널 연간 리뷰"),
    JOURNAL_TODO("JOURNAL_TODO", "저널 투두", "book"),

    SCHEDULE("SCHEDULE", "일정"),
    CHAT_MESSAGE("CHAT_MESSAGE", "채팅 메시지"),

    COMMENT("COMMENT", "댓글");

    public final String key;
    public final String desc;
    public String icon;

    /**
     * 문자 값과 일치하는 컨텐츠 타입 enum을 반환한다.
     *
     * @param contentType 문자 값
     * @return 컨텐츠 타입 enum
     */
    public static ContentType get(final String contentType) {
        for (final ContentType type : ContentType.values()) {
            if (type.key.equals(contentType)) return type;
        }
        return DEFAULT;
    }
}
