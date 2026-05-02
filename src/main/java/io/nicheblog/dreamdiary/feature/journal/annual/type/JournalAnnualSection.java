package io.nicheblog.dreamdiary.feature.journal.annual.type;

/**
 * JournalAnnualSection
 * 결산 상세 URL 등에서 쓰는 섹션(꿈·일기) 구분 enum.
 *
 * @author nichefish
 */
public enum JournalAnnualSection {
    DREAM,
    DIARY;

    /**
     * 대소문자를 무시하고 문자열을 enum 상수로 변환한다.
     *
     * @param value 요청 파라미터 등에서 넘어온 섹션 문자열
     * @return 대응하는 {@link JournalAnnualSection}
     */
    public static JournalAnnualSection from(final String value) {
        return JournalAnnualSection.valueOf(value.toUpperCase());
    }
}
