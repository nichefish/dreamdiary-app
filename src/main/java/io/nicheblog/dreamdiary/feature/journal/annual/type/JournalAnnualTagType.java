package io.nicheblog.dreamdiary.feature.journal.annual.type;

/**
 * JournalAnnualTagType
 * 결산 화면에서 태그 목록을 일자·꿈·일기 단위로 구분할 때 사용한다.
 *
 * @author nichefish
 */
public enum JournalAnnualTagType {
    DAY,
    DREAM,
    DIARY;

    /**
     * 대소문자를 무시하고 문자열을 enum 상수로 변환한다.
     *
     * @param value 요청 파라미터 등에서 넘어온 태그 구분 문자열
     * @return 대응하는 {@link JournalAnnualTagType}
     */
    public static JournalAnnualTagType from(final String value) {
        return JournalAnnualTagType.valueOf(value.toUpperCase());
    }
}
