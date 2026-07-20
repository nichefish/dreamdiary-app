package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.chat.model.RagIntent;
import org.apache.commons.lang3.StringUtils;

/**
 * 사용자 질문의 RAG 의도({@link RagIntent}) 휴리스틱 분류기.
 *
 * <p>{@link ChatAIService#detectRagIntent(String)} 대신 순수 규칙으로 LOOKUP/SUMMARY/SYNTHESIS를 결정한다.
 * 인물 about 판정({@code isPersonAboutLookupQuery})은 서비스에 남겨 플래그로 넘긴다.</p>
 *
 * <p>변경 전: {@code 최근} 단독으로 SUMMARY, {@code 패턴}/최근+찾기 동시 등으로 오분류가 쉽았다.
 * 변경 후: 명시적 검색 단서는 LOOKUP 우선, {@code 최근}은 요약 단서와 같이 쓸 때만 SUMMARY.</p>
 *
 * <p>{@link #needsLlmSecondPass(String)}은 SUMMARY·SYNTHESIS 단서가 동시에 잡힐 때만 true다.
 * {@link ChatAIService}가 그때 Ollama 2차 분류를 호출한다.</p>
 */
public final class RagIntentClassifier {

    /** 명시적 검색·찾기 단서. SYNTHESIS/SUMMARY 키워드보다 우선해 LOOKUP으로 보낸다. */
    private static final String[] LOOKUP_FORCE_HINTS = {
            "찾아줘", "찾아 줘", "찾아봐", "찾아 봐",
            "검색해", "검색해줘", "검색해 줘",
            "어디에 있", "어디 있", "어디에있"
    };

    /** 통섭·패턴·태도 질문 키워드 */
    private static final String[] SYNTHESIS_HINTS = {
            "의미", "통섭", "엷", "상징", "패턴", "흐름", "반복", "변화", "감정선",
            "어떤 존재", "어떤 역할", "어떻게 이어", "전체 맥락", "관통", "해석",
            "어떻게 생각", "생각하고", "어떤 감정", "어떤 마음", "어떤 느낌",
            "어떻게 느끼", "느끼고"
    };

    /** 요약·정리 동사 단서 ({@code 최근} 단독 제외) */
    private static final String[] SUMMARY_VERB_HINTS = {
            "요약", "정리", "모아", "묶어", "전체적으로", "한번에", "돌아봐"
    };

    /** {@code 최근} + 이 단서가 함께 있을 때만 SUMMARY */
    private static final String[] SUMMARY_RECENT_COMPANION_HINTS = {
            "요약", "정리", "모아", "묶어", "전체적으로", "한번에", "돌아봐",
            "전체", "흐름", "패턴", "반복"
    };

    private RagIntentClassifier() {
    }

    /**
     * 질문 문장으로 RAG 의도를 분류한다.
     *
     * @param queryText 사용자 질문
     * @param personAboutLookup 인물 토큰 + about 힌트가 있으면 true ({@link ChatAIService} 판정)
     * @return LOOKUP / SUMMARY / SYNTHESIS
     */
    public static RagIntent classify(final String queryText, final boolean personAboutLookup) {
        if (personAboutLookup) {
            return RagIntent.SYNTHESIS;
        }
        final String text = StringUtils.defaultString(queryText);
        if (StringUtils.isBlank(text)) {
            return RagIntent.LOOKUP;
        }
        // 명시적 검색 단서는 통섭/요약 키워드보다 우선 ("최근 ... 찾아줘" 오분류 방지).
        if (StringUtils.containsAny(text, LOOKUP_FORCE_HINTS)) {
            return RagIntent.LOOKUP;
        }
        if (StringUtils.containsAny(text, SYNTHESIS_HINTS)) {
            return RagIntent.SYNTHESIS;
        }
        if (isSummaryQuery(text)) {
            return RagIntent.SUMMARY;
        }
        return RagIntent.LOOKUP;
    }

    /**
     * SUMMARY·SYNTHESIS 단서가 동시에 잡혀 휴리스틱만으로는 모호할 때 true.
     *
     * <p>명시적 LOOKUP 강제 단서가 있으면 false (검색 우선 유지).</p>
     *
     * @param queryText 사용자 질문
     * @return LLM 2차 분류가 필요하면 true
     */
    public static boolean needsLlmSecondPass(final String queryText) {
        final String text = StringUtils.defaultString(queryText);
        if (StringUtils.isBlank(text)) {
            return false;
        }
        if (hasLookupForceHint(text)) {
            return false;
        }
        return hasSynthesisHint(text) && hasSummaryHint(text);
    }

    /** 명시적 검색·찾기 단서 여부. */
    static boolean hasLookupForceHint(final String text) {
        return StringUtils.containsAny(text, LOOKUP_FORCE_HINTS);
    }

    /** 통섭·패턴·태도 키워드 여부. */
    static boolean hasSynthesisHint(final String text) {
        return StringUtils.containsAny(text, SYNTHESIS_HINTS);
    }

    /** 요약형 단서 여부 ({@link #isSummaryQuery(String)}와 동일). */
    static boolean hasSummaryHint(final String text) {
        return isSummaryQuery(text);
    }

    /**
     * 요약형 질문인지 판단한다. {@code 최근} 단독은 SUMMARY가 아니다.
     */
    private static boolean isSummaryQuery(final String text) {
        if (StringUtils.containsAny(text, SUMMARY_VERB_HINTS)) {
            return true;
        }
        return StringUtils.contains(text, "최근")
                && StringUtils.containsAny(text, SUMMARY_RECENT_COMPANION_HINTS);
    }
}
