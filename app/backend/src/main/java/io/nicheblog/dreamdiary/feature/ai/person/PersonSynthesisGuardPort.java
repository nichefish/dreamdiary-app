package io.nicheblog.dreamdiary.feature.ai.person;

import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;

import java.util.List;

/**
 * Path C hybrid가 언어·person 저하 가드를 호출하는 포트.
 *
 * <p>구현체는 {@code feature.ai.guard.ResponseGuardService}이다. {@code feature.ai}는
 * {@code feature.chat}을 의존하지 않는다.</p>
 */
public interface PersonSynthesisGuardPort {

    /**
     * 응답에 허용되지 않는 Han script(또는 영어 locale의 과도한 한글)가 있는지 확인합니다.
     */
    boolean containsDisallowedHanScript(String text);

    /**
     * 언어 가드 실패 후 1회 재시도에 붙이는 지시문입니다.
     */
    String languageRetryPrompt();

    /**
     * person hollow/stance rich 등 저하 응답인지 판정합니다.
     */
    boolean isDegradedPersonResponse(
            String response,
            PersonFocus personFocus,
            List<RagSearchResult> results,
            RagIntent intent,
            String queryText
    );

    /**
     * person 가드 실패 시 UI/로그용 짧은 사유 코드를 반환합니다.
     */
    String describePersonGuardFailure(
            String response,
            PersonFocus personFocus,
            List<RagSearchResult> results,
            RagIntent intent,
            String queryText
    );
}
