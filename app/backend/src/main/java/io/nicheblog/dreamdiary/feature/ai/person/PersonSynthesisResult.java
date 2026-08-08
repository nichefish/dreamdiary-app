package io.nicheblog.dreamdiary.feature.ai.person;

/**
 * Path C person SYNTHESIS hybrid 경로의 최종 본문과 responseMode·가드 사유.
 *
 * <p>채널 오케스트레이터가 {@code ResolvedChatResponse}로 매핑한다.</p>
 *
 * @param content 사용자에게 보여줄 응답 본문
 * @param responseMode {@code PERSON_SYNTHESIS_HYBRID} / {@code RULE_PRIMARY} 등
 * @param guardDetail 1차 가드 실패 코드 (없으면 null)
 * @param retryGuardDetail 재시도 가드 실패 코드 (없으면 null)
 */
public record PersonSynthesisResult(
        String content,
        String responseMode,
        String guardDetail,
        String retryGuardDetail
) {
    /**
     * 가드 사유 없이 본문·모드만 담습니다.
     */
    public PersonSynthesisResult(final String content, final String responseMode) {
        this(content, responseMode, null, null);
    }

    /**
     * 1차 가드 사유만 담습니다.
     */
    public PersonSynthesisResult(final String content, final String responseMode, final String guardDetail) {
        this(content, responseMode, guardDetail, null);
    }
}
