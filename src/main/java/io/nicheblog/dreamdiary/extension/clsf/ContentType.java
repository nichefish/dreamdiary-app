package io.nicheblog.dreamdiary.extension.clsf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ContentType
 * <pre>
 *  ClsfEntity를 상속받은 클래스들이 사용하는 컨텐츠 타입 정보
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum ContentType {

    DEFAULT("DEFAULT", "기본"),
    // 공지사항
    NOTICE("NOTICE", "공지사항"),
    BOARD("BOARD", "일반게시판"),
    // 저널
    JRNL_DAY("JRNL_DAY", "저널 일자", "calendar3"),
    JRNL_DREAM("JRNL_DREAM", "저널 꿈", "moon-stars"),
    JRNL_DIARY("JRNL_DIARY", "저널 일기", "book"),
    JRNL_SBJCT("JRNL_SBJCT", "저널 주제"),
    JRNL_SUMRY("JRNL_SUMRY", "저널 결산"),
    JRNL_TODO("JRNL_TODO", "저널 할일", "book"),
    // 일정
    VCATN_PAPR("VCATN_PAPR", "휴가계획서"),
    SCHDUL("SCHDUL", "일정"),
    // 채팅
    CHAT_MSG("CHAT_MSG", "채팅 메세지"),

    //
    SECTN("SECTN", "단락"),
    COMMENT("COMMENT", "댓글"),
    FLSYS_META("FLSYS_META", "파일시스템 메타");

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