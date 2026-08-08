package io.nicheblog.dreamdiary.feature.ai.prompt;

import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 세션 기본 프롬프트에 응답 가드·RAG 블록·의도 프롬프트를 합성한다.
 *
 * <p>RAG 컨텍스트 본문({@code ragContextText})은 호출자가 구성해 넘긴다.
 * locale 키 {@code chat.ai.guard.response-rules}, {@code chat.ai.prompt.rag.journal-block}을 사용한다.</p>
 */
@Component
@RequiredArgsConstructor
public class SystemPromptBuilder {

    private final IntentPromptResolver intentPromptResolver;

    /**
     * 세션 기본 프롬프트보다 우선 적용할 응답 안전 규칙 (locale 카탈로그).
     *
     * @return 응답 가드 지시문
     */
    public String responseGuardPrompt() {
        return msg("chat.ai.guard.response-rules");
    }

    /**
     * 기본 시스템 프롬프트에 RAG 컨텍스트를 추가합니다.
     *
     * @param basePrompt 세션 또는 기본 시스템 프롬프트
     * @param intent RAG 의도 (null 허용)
     * @param ragContextText 저널 검색 결과 컨텍스트 본문 (blank면 가드만)
     * @param queryText 사용자 질문
     * @return RAG 컨텍스트가 포함된 최종 시스템 프롬프트
     */
    public String buildSystemPromptWithRag(
            final String basePrompt,
            final RagIntent intent,
            final String ragContextText,
            final String queryText
    ) {
        final String guardedPrompt = StringUtils.defaultString(basePrompt) + responseGuardPrompt();
        if (StringUtils.isBlank(ragContextText)) {
            return guardedPrompt;
        }
        return guardedPrompt
                + msg("chat.ai.prompt.rag.journal-block")
                + intentPromptResolver.buildIntentPrompt(intent, queryText)
                + "\n\n"
                + ragContextText;
    }

    private static String msg(final String key, final Object... args) {
        return MessageUtils.getMessage(key, args);
    }
}
