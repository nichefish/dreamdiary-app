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
    NOTICE("NOTICE", "공지사항"),
    BOARD("BOARD", "일반게시판"),

    JOURNAL_DAY("JOURNAL_DAY", "저널 일자", "calendar3"),
    JOURNAL_CHAPTER("JOURNAL_CHAPTER", "저널 챕터", "book"),
    JOURNAL_DIARY("JOURNAL_DIARY", "저널 일기", "book"),
    JOURNAL_DREAM("JOURNAL_DREAM", "저널 꿈", "moon-stars"),
    JOURNAL_NOTE("JOURNAL_NOTE", "저널 노트", "sticky"),
    JOURNAL_INTERPRETATION("JOURNAL_INTERPRETATION", "저널 해석", "book"),
    JOURNAL_SBJCT("JOURNAL_SBJCT", "저널 주제"),
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
     * 키와 일치하는 컨텐츠 타입 enum 반환
     * @param contentType 문자열
     * @return ContentType enum
     */
    public static ContentType get(final String contentType) {
        for (final ContentType type : ContentType.values()) {
            if (type.key.equals(contentType)) return type;
        }
        return DEFAULT;
    }
}
