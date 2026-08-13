package io.nicheblog.dreamdiary.feature.ai.guard;

import io.nicheblog.dreamdiary.feature.ai.person.PersonFocus;
import io.nicheblog.dreamdiary.feature.ai.person.PersonMeaningSnapshot;
import io.nicheblog.dreamdiary.feature.ai.person.PersonQueryClassifier;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSnapshotService;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSynthesisGuardPort;
import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 언어·person 응답 가드 구현.
 *
 * <p>{@link PersonSynthesisGuardPort}의 구현체이며 Path C hybrid와 chat LLM 경로가 공유한다.</p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class ResponseGuardService implements PersonSynthesisGuardPort {

    /** person-meaning 답변에 내부 스캐폴드/메타 필드명이 새어 나오면 degraded로 본다. */
    private static final String[] PERSON_MEANING_SCAFFOLD_LEAK_MARKERS = {
            "role_axes_ko", "roleaxesko", "repeated_tags", "PERSON_MEANING_SCAFFOLD",
            "1_반복축", "2_역할기능", "3_기록유형",
            "4_근거장면힌트", "5_확정불가", "entity catalog",
            "PERSON_STANCE_SCAFFOLD", "1_내태도", "2_반복패턴", "5_근거장면"
    };

    private final PersonSnapshotService personSnapshotService;

    /** LocaleContextHolder 기준 영어 UI 여부. Accept-Language en 과 axios 헤더에 대응한다. */
    private boolean isEnglishLocale() {
        final Locale locale = LocaleContextHolder.getLocale();
        return locale != null && locale.getLanguage().startsWith("en");
    }

    /**
     * 한국어 응답에 섞이면 안 되는 한자/중국어 계열 문자가 포함되었는지 확인합니다.
     *
     * <p>한국어 일반 응답에서 한자 1자는 우연히 포함될 수 있으므로 2자 이상부터 차단합니다.</p>
     */
    @Override
    public boolean containsDisallowedHanScript(final String text) {
        if (StringUtils.isBlank(text)) return false;
        if (isEnglishLocale()) {
            return containsExcessiveHangulForEnglish(text) || containsDisallowedHanScriptCount(text, 2);
        }
        return containsDisallowedHanScriptCount(text, 2);
    }

    boolean containsDisallowedHanScriptCount(final String text, final int threshold) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeScript.of(text.charAt(i)) == Character.UnicodeScript.HAN) {
                count++;
                if (count >= threshold) return true;
            }
        }
        return false;
    }

    /** 영어 locale 에서 한국어 본문이 과도하게 섞였는지 확인한다. */
    boolean containsExcessiveHangulForEnglish(final String text) {
        int hangul = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeScript.of(text.charAt(i)) == Character.UnicodeScript.HANGUL) {
                hangul++;
                if (hangul >= 12) return true;
            }
        }
        return false;
    }


    /**
     * 언어 가드 실패 후 1회 재시도에 붙이는 지시문입니다.
     */
    @Override
    public String languageRetryPrompt() {
        return MessageUtils.getMessage("chat.ai.guard.language-retry");
    }

    /**
     * person-meaning 통섭 답변이 빈 분류·스캐폴드 유출·무관 태그 인용만 있는지 검사합니다.
     */
    public boolean isHollowPersonMeaningResponse(
            final String response,
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final RagIntent intent,
            final String queryText
    ) {
        if (intent != RagIntent.SYNTHESIS || personFocus == null) {
            return false;
        }
        if (StringUtils.isBlank(response)) return true;
        if (isPersonMeaningScaffoldLeak(response)) return true;
        if (PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
            return isDegradedPersonStanceRichResponse(response);
        }
        if (isThirdPersonPersonalityProfile(response)) return true;
        if (isPersonMeaningQuoteParade(response)) return true;
        return !hasPersonMeaningEvidence(response, personFocus, results, queryText);
    }

    /**
     * 인용문·대사 나열만 있고 해석이 없는 person-meaning 답변을 감지합니다.
     */
    boolean isPersonMeaningQuoteParade(final String response) {
        if (StringUtils.isBlank(response)) return false;
        final boolean hasQuoteParadeCue = StringUtils.containsAny(response,
                "말을 하면서", "라고 말", "라고 했", "또는 \"", "....\"");
        if (!hasQuoteParadeCue) return false;
        return StringUtils.containsAny(response,
                "추론하자면", "추론하면", "인 것 같", "인물로", "자연스러운");
    }

    /**
     * person 가드 실패 시 UI/로그용 짧은 사유 코드를 반환합니다.
     */
    @Override
    public String describePersonGuardFailure(
            final String response,
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final RagIntent intent,
            final String queryText
    ) {
        if (StringUtils.isBlank(response)) return "empty_response";
        if (containsDisallowedHanScript(response)) return "language_guard";
        if (PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
            return describePersonStanceRichGuardFailure(response);
        }
        if (isHollowPersonMeaningResponse(response, personFocus, results, intent, queryText)) {
            return "person_meaning_hollow";
        }
        return "person_guard_rejected";
    }

    /**
     * 상대 성격을 3인칭으로 단정하는 HR식 프로필 답변을 감지합니다.
     */
    boolean isThirdPersonPersonalityProfile(final String response) {
        if (StringUtils.isBlank(response)) return false;
        return StringUtils.containsAny(response,
                "열성적", "주도적", "인물입니다", "인물이며",
                "중요한 역할을", "주도적인 인물",
                "친근하고", "친근한 인물", "자연스러운 인물",
                "추론하자면", "추론하면");
    }

    /**
     * SYNTHESIS person-meaning hollow guard와 LOOKUP 인물 태도 질문의 빈 주제 분류를 함께 검사합니다.
     */
    @Override
    public boolean isDegradedPersonResponse(
            final String response,
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final RagIntent intent,
            final String queryText
    ) {
        if (isHollowPersonMeaningResponse(response, personFocus, results, intent, queryText)) {
            return true;
        }
        if (intent != RagIntent.LOOKUP) {
            return false;
        }
        if (!PersonQueryClassifier.isPersonMeaningQuery(queryText)
                || PersonQueryClassifier.extractPersonFocusTokens(queryText).isEmpty()) {
            return false;
        }
        return isGenericPersonBucketHallucination(response);
    }

    /**
     * 기록 근거 없이 조직·업무 같은 일반 분류만 나열한 LOOKUP 인물 답변을 감지합니다.
     */
    boolean isGenericPersonBucketHallucination(final String response) {
        if (StringUtils.isBlank(response)) return true;

        final boolean hasGenericBucket = StringUtils.containsAny(response,
                "조직 내", "조직 속", "조직에서",
                "업무 협업", "사내 문화", "존재감",
                "중요한 역할", "전략적");
        if (!hasGenericBucket) return false;

        return !StringUtils.containsAny(response, "#", "기록상", "반복", "태그");
    }

    /**
     * 태도 질문 "풍부 신뢰" 모드(Option A)의 최소 거부 게이트.
     *
     * <p>형식(4섹션)·코칭/조직 톤·3인칭 서술·에피소드 나열·인용은 더 이상 거부 사유가 아니다.
     * 큰 SNAPSHOT을 읽고 자연스러운 산문으로 태도를 서술하도록 허용하며, 빈 응답·너무 짧은 답·
     * 스캐폴드 유출·기록 근거 없는 일반 버킷 나열만 거부한다. 한글 언어 가드는 상위 흐름에서 별도 처리한다.</p>
     */
    public boolean isDegradedPersonStanceRichResponse(final String response) {
        if (StringUtils.isBlank(response)) return true;
        if (StringUtils.length(StringUtils.normalizeSpace(response)) < 40) return true;
        if (isPersonMeaningScaffoldLeak(response)) return true;
        return isGenericPersonBucketHallucination(response);
    }

    /**
     * 태도 질문 "풍부 신뢰" 모드에서 거부 사유 코드를 반환한다.
     *
     * <p>{@link #isDegradedPersonStanceRichResponse(String)}와 동일한 판정을 코드로 표현한다.</p>
     */
    public String describePersonStanceRichGuardFailure(final String response) {
        if (StringUtils.isBlank(response)
                || StringUtils.length(StringUtils.normalizeSpace(response)) < 40) {
            return "person_stance_too_short";
        }
        if (isPersonMeaningScaffoldLeak(response)) return "person_stance_scaffold_leak";
        if (isGenericPersonBucketHallucination(response)) return "person_stance_generic_bucket";
        return "person_guard_rejected";
    }

    /**
     * person-meaning 답변에 내부 스캐폴드/메타 필드명이 새어 나왔는지 확인합니다.
     */
    boolean isPersonMeaningScaffoldLeak(final String response) {
        if (StringUtils.isBlank(response)) return false;
        final String normalized = StringUtils.lowerCase(response);
        for (final String marker : PERSON_MEANING_SCAFFOLD_LEAK_MARKERS) {
            if (normalized.contains(StringUtils.lowerCase(marker))) return true;
        }
        return false;
    }


    /**
     * person-meaning 답변이 이번 질문의 스냅샷 태그·역할 축·연결 맥락을 실제로 인용했는지 확인합니다.
     */
    boolean hasPersonMeaningEvidence(
            final String response,
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final String queryText
    ) {
        if (StringUtils.isBlank(response) || personFocus == null) {
            return false;
        }

        final PersonMeaningSnapshot snapshot = personSnapshotService.build(
                results == null ? List.of() : results,
                personFocus,
                queryText
        );
        if (citesPersonMeaningTagEvidence(response, snapshot.repeatedTagCountMap())) return true;
        if (citesPersonMeaningRoleAxisEvidence(response, snapshot.roleAxesKo())) return true;
        if (citesPersonMeaningTagEvidence(response, snapshot.linkedContextTagCountMap())) return true;
        if (citesPersonMeaningChapterPrefixEvidence(response, snapshot.chapterPrefixCountMap())) return true;
        if (citesPersonMeaningContentKindEvidence(response, snapshot.contentKindCountMap())) return true;
        return citesPersonMeaningSnippetEvidence(response, snapshot.evidenceSnippets());
    }

    /**
     * person-meaning 답변이 태그 전체 문자열 또는 # 후 핵심어를 인용했는지 확인합니다.
     */
    public boolean citesPersonMeaningTagEvidence(final String response, final Map<String, Integer> tagCountMap) {
        if (StringUtils.isBlank(response) || tagCountMap == null || tagCountMap.isEmpty()) return false;

        for (final String tag : tagCountMap.keySet()) {
            if (StringUtils.isBlank(tag)) continue;
            if (StringUtils.contains(response, tag)) return true;

            final String probe = extractTagCitationProbe(tag);
            if (StringUtils.length(probe) >= 2 && StringUtils.contains(response, probe)) {
                return true;
            }
        }
        return false;
    }

    /**
     * person-meaning 답변이 역할 축 라벨을 인용했는지 확인합니다.
     */
    boolean citesPersonMeaningRoleAxisEvidence(final String response, final List<String> roleAxesKo) {
        if (StringUtils.isBlank(response) || roleAxesKo == null || roleAxesKo.isEmpty()) return false;

        for (final String roleAxis : roleAxesKo) {
            if (StringUtils.contains(response, roleAxis)) return true;
            final int parenIndex = roleAxis.indexOf('(');
            if (parenIndex > 0 && StringUtils.contains(response, roleAxis.substring(0, parenIndex))) {
                return true;
            }
        }
        return false;
    }

    /**
     * person-meaning 답변이 챕터 말머리를 인용했는지 확인합니다.
     */
    boolean citesPersonMeaningChapterPrefixEvidence(
            final String response,
            final Map<String, Integer> chapterPrefixCountMap
    ) {
        if (StringUtils.isBlank(response) || chapterPrefixCountMap == null || chapterPrefixCountMap.isEmpty()) {
            return false;
        }

        for (final String chapterPrefix : chapterPrefixCountMap.keySet()) {
            if (StringUtils.isNotBlank(chapterPrefix)
                    && StringUtils.containsIgnoreCase(response, chapterPrefix)) return true;
        }
        return false;
    }

    /**
     * person-meaning 답변이 기록 유형(꾋/일기/노트) 표현을 인용했는지 확인합니다.
     */
    boolean citesPersonMeaningContentKindEvidence(
            final String response,
            final Map<String, Integer> contentKindCountMap
    ) {
        if (StringUtils.isBlank(response) || contentKindCountMap == null || contentKindCountMap.isEmpty()) {
            return false;
        }

        if (contentKindCountMap.getOrDefault("DREAM", 0) > 0
                && StringUtils.containsAny(response, "꾋", "DREAM")) {
            return true;
        }
        if (contentKindCountMap.getOrDefault("DIARY", 0) > 0
                && StringUtils.containsAny(response, "일기", "DIARY")) {
            return true;
        }
        return contentKindCountMap.getOrDefault("NOTE", 0) > 0
                && StringUtils.containsAny(response, "노트", "NOTE");
    }

    /**
     * 태그 문자열에서 guard 인용 확인용 # 후 핵심어를 추출합니다.
     */
    String extractTagCitationProbe(final String tag) {
        if (StringUtils.isBlank(tag)) return "";

        final int hashIndex = tag.indexOf('#');
        if (hashIndex >= 0 && hashIndex < tag.length() - 1) {
            return StringUtils.trim(tag.substring(hashIndex + 1));
        }
        return StringUtils.trim(tag);
    }

    /**
     * person 태그·역할 축이 없을 때 근거 스니펫 인용 여부를 확인합니다.
     */
    public boolean citesPersonMeaningSnippetEvidence(final String response, final List<String> evidenceSnippets) {
        if (StringUtils.isBlank(response) || evidenceSnippets == null || evidenceSnippets.isEmpty()) return false;

        for (final String snippet : evidenceSnippets) {
            for (final String probe : extractSnippetEvidenceProbes(snippet)) {
                if (StringUtils.isNotBlank(probe) && StringUtils.containsIgnoreCase(response, probe)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extracts multiple short probes from a sanitized snippet for hollow-guard citation checks.
     */
    List<String> extractSnippetEvidenceProbes(final String snippet) {
        if (StringUtils.isBlank(snippet)) return List.of();

        final String normalized = StringUtils.normalizeSpace(StringUtils.defaultString(snippet).replace("...", " "));
        if (StringUtils.isBlank(normalized)) return List.of();

        final List<String> probes = new ArrayList<>();
        probes.add(normalized);
        if (normalized.length() > 12) {
            probes.add(normalized.substring(0, Math.min(20, normalized.length())).trim());
            final int start = Math.max(0, (normalized.length() - 15) / 2);
            probes.add(normalized.substring(start, Math.min(normalized.length(), start + 15)).trim());
        }
        return probes.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
    }
}
