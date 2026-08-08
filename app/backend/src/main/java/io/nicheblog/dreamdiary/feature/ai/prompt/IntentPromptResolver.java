package io.nicheblog.dreamdiary.feature.ai.prompt;

import io.nicheblog.dreamdiary.feature.ai.person.PersonQueryClassifier;
import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.springframework.stereotype.Component;

/**
 * RAG 의도·질문 유형별 추가 시스템 프롬프트를 해석한다.
 *
 * <p>locale 카탈로그 {@code chat.ai.prompt.intent.*} 키를 사용한다.</p>
 */
@Component
public class IntentPromptResolver {

    /**
     * RAG 의도별 추가 프롬프트를 구성합니다.
     *
     * <p>태도 질문이 레거시 일반 경로에 오는 것은 personFocus 미해결(태그·기록·entity 단서가 없는
     * 인물) 케이스뿐이다. Path C hybrid 프롬프트와 같은 rich-trust 계약(자유 산문·2인칭 비춤·반환각만
     * 금지)을 적용하며, 예전 4개 항목 스캐폴드 골격·조언/톤 금지 레짐은 재도입하지 않는다.</p>
     *
     * <p>PERSON_FOCUS·PERSON_MEANING_SCAFFOLD 블록 참조는 제거됨(convergence): personFocus가 해결된
     * 질문은 Path C(SNAPSHOT 프롬프트)로 가므로, 이 분기(레거시 일반 경로)에서는 블록이 존재할 수 없다.</p>
     *
     * @param intent RAG 의도
     * @param queryText 사용자 질문
     * @return 의도별 추가 프롬프트 본문
     */
    public String buildIntentPrompt(final RagIntent intent, final String queryText) {
        if (intent == RagIntent.SYNTHESIS && PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
            return msg("chat.ai.prompt.intent.stance");
        }
        if (intent == RagIntent.SYNTHESIS
                && PersonQueryClassifier.isPersonAppearanceQuery(queryText)
                && PersonQueryClassifier.isPersonMeaningQuery(queryText)) {
            return msg("chat.ai.prompt.intent.appearance-meaning");
        }
        if (intent == RagIntent.SYNTHESIS) {
            return msg("chat.ai.prompt.intent.synthesis");
        }
        if (intent == RagIntent.SUMMARY) {
            return msg("chat.ai.prompt.intent.summary");
        }
        if (intent == RagIntent.LOOKUP && PersonQueryClassifier.isPersonLookupQuery(queryText)) {
            return msg("chat.ai.prompt.intent.lookup-person");
        }
        return msg("chat.ai.prompt.intent.default");
    }

    /**
     * SUMMARY∩SYNTHESIS 모호 시 LLM 2차 의도 분류용 시스템 프롬프트.
     *
     * @return intent-classify 지시문
     */
    public String intentClassifyPrompt() {
        return msg("chat.ai.prompt.intent-classify");
    }

    private static String msg(final String key, final Object... args) {
        return MessageUtils.getMessage(key, args);
    }
}
