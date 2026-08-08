package io.nicheblog.dreamdiary.feature.ai.person;

import io.nicheblog.dreamdiary.feature.ai.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.ai.model.AiChatMessage;
import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Path C person SYNTHESIS hybrid 오케스트레이션.
 *
 * <p>스냅샷 프롬프트·LLM 호출·rule-primary 폴백·재시도 프롬프트를 담당한다.
 * 언어·person 가드 구현은 {@link PersonSynthesisGuardPort}로 주입받으며,
 * 세션 메시지 로딩은 chat 오케스트레이터가 수행한다.</p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class PersonSynthesisHybridService {

    private final OllamaClient ollamaClient;
    private final PersonSnapshotService personSnapshotService;
    private final PersonFocusResolver personFocusResolver;


    /**
     * chat.ai.* 카탈로그 메시지를 현재 locale로 조회한다.
     *
     * <p>{@link MessageUtils#getMessage(String, Object[])}는 가변인자가 아니라 배열을 받으므로
     * hybrid 서비스에서 varargs 헬퍼로 감싼다.</p>
     */
    private String msg(final String key, final Object... args) {
        return MessageUtils.getMessage(key, args);
    }


    /**
     * SYNTHESIS person 질문은 스냅샷 hybrid(LLM 해석 + rule-primary 폴백) 경로를 탑니다.
     */
    public boolean shouldUsePathC(
            final PersonFocus personFocus,
            final RagIntent intent,
            final String queryText
    ) {
        return intent == RagIntent.SYNTHESIS
                && personFocus != null
                && PersonQueryClassifier.isPersonMeaningQuery(queryText);
    }

    /**
     * person SYNTHESIS: 서버 스냅샷 집계 후 LLM 해석 1회를 시도하고, 가드 실패 시 RULE_PRIMARY로 폴백합니다.
     *
     * <p>세션 메시지 로딩은 chat 오케스트레이터가 담당하며, 이미 변환된 {@link AiChatMessage} 목록을 받는다.</p>
     */
    public PersonSynthesisResult resolveHybrid(
            final Integer sessionId,
            final String message,
            final RagIntent intent,
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final List<AiChatMessage> context,
            final PersonSynthesisGuardPort guard
    ) throws Exception {
        final String rulePrimaryFallback = buildRulePrimaryPersonSynthesisResponse(personFocus, results, message);
        if (personFocus == null
                || results == null
                || results.isEmpty()) {
            return new PersonSynthesisResult(rulePrimaryFallback, "RULE_PRIMARY", "empty_tagged_sources");
        }

        final String systemPrompt = buildHybridSystemPrompt(personFocus, results, message);
        final List<AiChatMessage> hybridContext = context == null ? List.of() : context;

        String rawResponse = ollamaClient.chat(systemPrompt, hybridContext);
        if (guard.containsDisallowedHanScript(rawResponse)) {
            log.warn("AI person synthesis hybrid language guard retry. sessionId={}", sessionId);
            rawResponse = ollamaClient.chat(systemPrompt + guard.languageRetryPrompt(), hybridContext);
        }

        String strippedResponse = stripInternalRecordCitations(rawResponse);
        if (guard.containsDisallowedHanScript(strippedResponse)) {
            log.warn("AI person synthesis hybrid language fallback to rule-primary. sessionId={}", sessionId);
            return new PersonSynthesisResult(rulePrimaryFallback, "RULE_PRIMARY", "language_guard");
        }
        if (!guard.isDegradedPersonResponse(strippedResponse, personFocus, results, intent, message)) {
            return new PersonSynthesisResult(strippedResponse, "PERSON_SYNTHESIS_HYBRID", null);
        }

        final String firstGuardDetail = guard.describePersonGuardFailure(
                strippedResponse, personFocus, results, intent, message);
        log.warn("AI person synthesis hybrid degraded, retrying once. sessionId={}, guardDetail={}",
                sessionId, firstGuardDetail);
        final String retryResponse = stripInternalRecordCitations(ollamaClient.chat(
                systemPrompt + buildPersonMeaningRetryPrompt(personFocus, results, message, firstGuardDetail),
                hybridContext
        ));
        if (!guard.containsDisallowedHanScript(retryResponse)
                && !guard.isDegradedPersonResponse(retryResponse, personFocus, results, intent, message)) {
            return new PersonSynthesisResult(retryResponse, "PERSON_SYNTHESIS_HYBRID", null);
        }

        final String retryGuardDetail = guard.describePersonGuardFailure(
                retryResponse, personFocus, results, intent, message);
        log.warn("AI person synthesis hybrid guard failed, rule-primary fallback. sessionId={}, guardDetail={}, retryGuardDetail={}",
                sessionId, firstGuardDetail, retryGuardDetail);
        return new PersonSynthesisResult(rulePrimaryFallback, "RULE_PRIMARY", firstGuardDetail, retryGuardDetail);
    }

    /**
     * AI 응답에서 RAG 내부 기록 인덱스([1], [2] 등) 인용을 제거합니다. 마크다운 기호는 보존하며 클라이언트에서 HTML로 렌더합니다.
     */
    private String stripInternalRecordCitations(final String text) {
        if (text == null) return null;
        return text
                .replaceAll("\\[(\\d{1,2})\\]\\s*기록", "기록")
                .replaceAll("\\[(\\d{1,2})\\]", "")
                .replaceAll(" {2,}", " ")
                .replaceAll(" (?=[.,!?])", "")
                .trim();
    }

    /**
     * person SYNTHESIS 질문 유형에 맞는 규칙 기반 응답을 조립합니다.
     */
    public String buildRulePrimaryPersonSynthesisResponse(
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final String queryText
    ) {
        if (PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
            return buildPersonStanceDeterministicFallback(personFocus, results, queryText);
        }
        if (PersonQueryClassifier.isPersonAppearanceQuery(queryText)) {
            return buildPersonAppearanceDeterministicFallback(personFocus, results);
        }
        return buildPersonMeaningDeterministicFallback(personFocus, results);
    }

    /**
     * person SYNTHESIS hybrid 경로용 시스템 프롬프트를 만듭니다.
     *
     * <p>전체 RAG 덤프 대신 {@link PersonMeaningSnapshot}만 전달합니다. 태도 질문은 rich-trust 자유 산문을
     * 안내하고 최소 게이트만 적용합니다.
     * 등장(appearance) 질문은 현행대로 4섹션 형식을 프롬프트로 안내합니다.</p>
     */
    public String buildHybridSystemPrompt(
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final String queryText
    ) {
        final StringBuilder sb = new StringBuilder();
        sb.append(msg("chat.ai.guard.response-rules"));
        sb.append(msg("chat.ai.prompt.rag.hybrid.header"));
        sb.append(msg("chat.ai.prompt.rag.hybrid.rule.snapshot-only"));
        sb.append(msg("chat.ai.prompt.rag.hybrid.rule.no-internal-fields"));
        sb.append(msg("chat.ai.prompt.rag.hybrid.rule.tag-hash-only"));
        sb.append(msg("chat.ai.prompt.rag.hybrid.rule.context-followup"));

        if (PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
            sb.append(msg("chat.ai.prompt.rag.hybrid.stance.opening"));
            sb.append(msg("chat.ai.prompt.rag.hybrid.stance.evidence"));
            sb.append(msg("chat.ai.prompt.rag.hybrid.stance.format-free"));
            sb.append(msg("chat.ai.prompt.rag.hybrid.stance.ground-only"));
            sb.append(msg("chat.ai.prompt.rag.hybrid.stance.continue-interpretation"));
            sb.append(msg("chat.ai.prompt.rag.hybrid.stance.length"));
        } else if (PersonQueryClassifier.isPersonAppearanceQuery(queryText)) {
            sb.append(msg("chat.ai.prompt.rag.hybrid.appearance.question"));
            sb.append(msg("chat.ai.prompt.rag.hybrid.appearance.format"));
            sb.append(msg("chat.ai.prompt.rag.hybrid.appearance.forbidden"));
        } else {
            sb.append(msg("chat.ai.prompt.rag.hybrid.meaning.question"));
            sb.append(msg("chat.ai.prompt.rag.hybrid.meaning.guide"));
        }

        sb.append(msg("chat.ai.prompt.rag.hybrid.snapshot.header"));
        appendPersonSynthesisSnapshotBlock(sb, personFocus, results, queryText);
        return sb.toString().trim();
    }

    /**
     * hybrid LLM에 전달할 person-meaning 스냅샷 블록을 조립합니다.
     */
    private void appendPersonSynthesisSnapshotBlock(
            final StringBuilder sb,
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final String queryText
    ) {
        if (personFocus == null) return;

        final PersonMeaningSnapshot snapshot = personSnapshotService.build(results, personFocus, queryText);
        final String target = personFocusResolver.resolvePersonFocusTarget(personFocus);

        sb.append(msg("chat.ai.prompt.rag.hybrid.snapshot.label.target"))
                .append(": ")
                .append(StringUtils.defaultString(target))
                .append('\n');

        if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append(msg("chat.ai.prompt.rag.hybrid.snapshot.label.repeated-tags"))
                    .append(": ")
                    .append(formatTopTagsForDisplay(snapshot.repeatedTagCountMap(), 8))
                    .append('\n');
        }
        if (!snapshot.linkedContextTagCountMap().isEmpty()) {
            sb.append(msg("chat.ai.prompt.rag.hybrid.snapshot.label.linked-context"))
                    .append(": ")
                    .append(formatTopTagsForDisplay(snapshot.linkedContextTagCountMap(), 8))
                    .append('\n');
        }
        if (!snapshot.chapterPrefixCountMap().isEmpty()) {
            sb.append(msg("chat.ai.prompt.rag.hybrid.snapshot.label.chapter-prefix"))
                    .append(": ")
                    .append(formatChapterPrefixSpread(snapshot.chapterPrefixCountMap()))
                    .append('\n');
        }
        if (!snapshot.roleAxesKo().isEmpty()) {
            sb.append(msg("chat.ai.prompt.rag.hybrid.snapshot.label.role-axes"))
                    .append(": ")
                    .append(String.join(", ", snapshot.roleAxesKo()))
                    .append('\n');
        }
        if (!snapshot.contentKindCountMap().isEmpty()) {
            sb.append(msg("chat.ai.prompt.rag.hybrid.snapshot.label.content-kind"))
                    .append(": ")
                    .append(formatContentKindSpread(snapshot.contentKindCountMap()))
                    .append('\n');
        }
        if (StringUtils.isNotBlank(snapshot.firstDate()) || StringUtils.isNotBlank(snapshot.lastDate())) {
            sb.append(msg(
                    "chat.ai.prompt.rag.hybrid.snapshot.label.period",
                    StringUtils.defaultIfBlank(snapshot.firstDate(), "?"),
                    StringUtils.defaultIfBlank(snapshot.lastDate(), "?")
            ));
        }
        if (!snapshot.evidenceSnippets().isEmpty()) {
            final String evidenceLabel = PersonQueryClassifier.isPersonAttitudeQuery(queryText)
                    ? msg("chat.ai.prompt.rag.hybrid.snapshot.label.evidence")
                    : msg("chat.ai.prompt.rag.hybrid.snapshot.label.evidence-short");
            sb.append(evidenceLabel).append(": ")
                    .append(String.join(" | ", snapshot.evidenceSnippets()))
                    .append('\n');
        }

        final String interpretiveSeed;
        if (PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
            interpretiveSeed = buildPersonStanceInterpretiveLead(
                    target,
                    snapshot.repeatedTagCountMap(),
                    snapshot.roleAxesKo(),
                    snapshot.contentKindCountMap(),
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterPrefixCountMap()
            );
        } else if (PersonQueryClassifier.isPersonAppearanceQuery(queryText)) {
            interpretiveSeed = buildPersonAppearanceInterpretiveLead(
                    target,
                    snapshot.repeatedTagCountMap(),
                    snapshot.roleAxesKo(),
                    snapshot.contentKindCountMap(),
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterPrefixCountMap()
            );
        } else {
            interpretiveSeed = buildPersonMeaningInterpretiveLead(
                    target,
                    snapshot.repeatedTagCountMap(),
                    snapshot.roleAxesKo(),
                    snapshot.contentKindCountMap(),
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterPrefixCountMap()
            );
        }
        if (StringUtils.isNotBlank(interpretiveSeed)) {
            sb.append(msg("chat.ai.prompt.rag.hybrid.snapshot.label.interpretive-seed"))
                    .append(": ")
                    .append(interpretiveSeed)
                    .append('\n');
        }
    }

    /**
     * 상위 태그 카운트를 프롬프트에 넣기 좋은 문자열로 변환합니다.
     */
    private String formatTopTags(final Map<String, Integer> tagCountMap, final int limit) {
        return tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> entry.getKey() + "(" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }


    /**
     * 규칙 기반 person 응답에 쓸 태그 요약 문자열입니다.
     */
    private String formatTopTagsForDisplay(final Map<String, Integer> tagCountMap, final int limit) {
        return tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> personFocusResolver.formatDisplayTag(entry.getKey()) + "(" + entry.getValue() + ")")
                .filter(entry -> StringUtils.isNotBlank(entry) && !entry.startsWith("("))
                .collect(Collectors.joining(", "));
    }

    /**
     * 1차 답변이 거부된 구체 사유를 재시도 프롬프트에 반영합니다.
     *
     * <p>describePersonGuardFailure 코드와 동일한 토큰을 사용합니다.</p>
     */
    private void appendPersonGuardRetryHint(final StringBuilder sb, final String guardDetail) {
        if (StringUtils.isBlank(guardDetail)) {
            return;
        }
        sb.append(msg("chat.ai.prompt.rag.retry.guard-detail-label", guardDetail));
        switch (guardDetail) {
            case "person_stance_too_short" -> sb.append(
                    msg("chat.ai.prompt.rag.retry.hint.person-stance-too-short"));
            case "person_stance_scaffold_leak" -> sb.append(
                    msg("chat.ai.prompt.rag.retry.hint.person-stance-scaffold-leak"));
            case "person_stance_generic_bucket" -> sb.append(
                    msg("chat.ai.prompt.rag.retry.hint.person-stance-generic-bucket"));
            case "person_meaning_hollow" -> sb.append(
                    msg("chat.ai.prompt.rag.retry.hint.person-meaning-hollow"));
            case "language_guard" -> sb.append(
                    msg("chat.ai.prompt.rag.retry.hint.language-guard"));
            case "empty_response" -> sb.append(
                    msg("chat.ai.prompt.rag.retry.hint.empty-response"));
            default -> sb.append(msg("chat.ai.prompt.rag.retry.hint.default"));
        }
    }

    /**
     * Builds a one-shot retry prompt when person-meaning hollow guard rejects the first LLM answer.
     */
    public String buildPersonMeaningRetryPrompt(
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final String queryText,
            final String guardDetail
    ) {
        if (PersonQueryClassifier.isPersonAttitudeQuery(queryText)) {
            return buildPersonStanceRetryPrompt(personFocus, results, queryText, guardDetail);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(msg("chat.ai.prompt.rag.retry.meaning.header"));
        appendPersonGuardRetryHint(sb, guardDetail);
        sb.append(msg("chat.ai.prompt.rag.retry.meaning.rejected"));
        sb.append(msg("chat.ai.prompt.rag.retry.meaning.cite-required"));
        sb.append(msg("chat.ai.prompt.rag.retry.meaning.no-generic-bucket"));

        if (personFocus != null) {
            final PersonMeaningSnapshot snapshot = personSnapshotService.build(results, personFocus, null);
            if (!snapshot.repeatedTagCountMap().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.person-tags"))
                        .append(": ")
                        .append(formatTopTags(snapshot.repeatedTagCountMap(), 4))
                        .append('\n');
            }
            if (!snapshot.linkedContextTagCountMap().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.linked-context"))
                        .append(": ")
                        .append(formatTopTags(snapshot.linkedContextTagCountMap(), 4))
                        .append('\n');
            }
            if (!snapshot.chapterPrefixCountMap().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.chapter-prefix"))
                        .append(": ")
                        .append(formatChapterPrefixSpread(snapshot.chapterPrefixCountMap()))
                        .append('\n');
            }
            if (!snapshot.contentKindCountMap().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.content-kind"))
                        .append(": ")
                        .append(formatContentKindSpread(snapshot.contentKindCountMap()))
                        .append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * 태도 질문 "풍부 신뢰"(Option A) 답이 rich 게이트(빈 일반론·너무 짧음·스캐폴드 유출)로 거부됐을 때 1회 재시도 프롬프트.
     *
     * <p>변경 전: 4섹션 형식·anti-톤 규제를 다시 강제. 변경 후: 형식 강제를 없애고 근거 장면 인용과
     * 2인칭 비춤만 요구해 Option A 계약과 일치시킨다.</p>
     */
    /**
     * 태도 질문 "풍부 신뢰"(Option A) 답이 rich 게이트로 거부됐을 때 1회 재시도 프롬프트.
     *
     * <p>형식 강제를 없애고 근거 장면 인용과 2인칭 비춤만 요구해 Option A 계약과 일치시킨다.</p>
     */
    private String buildPersonStanceRetryPrompt(
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final String queryText,
            final String guardDetail
    ) {
        final StringBuilder sb = new StringBuilder();
        sb.append(msg("chat.ai.prompt.rag.retry.stance.header"));
        appendPersonGuardRetryHint(sb, guardDetail);
        sb.append(msg("chat.ai.prompt.rag.retry.stance.weak-evidence"));
        sb.append(msg("chat.ai.prompt.rag.retry.stance.honest-summary"));
        sb.append(msg("chat.ai.prompt.rag.retry.stance.opening-format"));
        sb.append(msg("chat.ai.prompt.rag.retry.stance.no-fabrication"));

        if (personFocus != null) {
            final PersonMeaningSnapshot snapshot = personSnapshotService.build(results, personFocus, queryText);
            final String interpretiveLead = buildPersonStanceInterpretiveLead(
                    personFocusResolver.resolvePersonFocusTarget(personFocus),
                    snapshot.repeatedTagCountMap(),
                    snapshot.roleAxesKo(),
                    snapshot.contentKindCountMap(),
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterPrefixCountMap()
            );
            if (StringUtils.isNotBlank(interpretiveLead)) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.interpretive-seed"))
                        .append(": ")
                        .append(interpretiveLead)
                        .append('\n');
            }
            if (!snapshot.repeatedTagCountMap().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.person-tags"))
                        .append(": ")
                        .append(formatTopTags(snapshot.repeatedTagCountMap(), 6))
                        .append('\n');
            }
            if (!snapshot.linkedContextTagCountMap().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.linked-context"))
                        .append(": ")
                        .append(formatTopTags(snapshot.linkedContextTagCountMap(), 4))
                        .append('\n');
            }
            if (!snapshot.roleAxesKo().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.role-axes"))
                        .append(": ")
                        .append(String.join(", ", snapshot.roleAxesKo()))
                        .append('\n');
            }
            if (!snapshot.contentKindCountMap().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.content-kind"))
                        .append(": ")
                        .append(formatContentKindSpread(snapshot.contentKindCountMap()))
                        .append('\n');
            }
            if (!snapshot.evidenceSnippets().isEmpty()) {
                sb.append(msg("chat.ai.prompt.rag.retry.label.evidence"))
                        .append(": ")
                        .append(String.join(" | ", snapshot.evidenceSnippets()))
                        .append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Appends role/function text from linked context tags and chapter Prefix values.
     */
    private void appendPersonMeaningLinkedContextRoleHint(
            final StringBuilder sb,
            final Map<String, Integer> linkedContextTagCountMap,
            final Map<String, Integer> chapterPrefixCountMap,
            final boolean scaffoldStyle
    ) {
        if ((linkedContextTagCountMap == null || linkedContextTagCountMap.isEmpty())
                && (chapterPrefixCountMap == null || chapterPrefixCountMap.isEmpty())) {
            if (scaffoldStyle) {
                sb.append(msg("chat.ai.fallback.linked-context.scaffold-empty"));
            } else {
                sb.append(msg("chat.ai.fallback.linked-context.empty"));
            }
            return;
        }

        if (!scaffoldStyle) {
            sb.append(msg("chat.ai.fallback.linked-context.prefix")).append(" ");
        }
        final List<String> hintParts = new ArrayList<>();
        if (linkedContextTagCountMap != null && !linkedContextTagCountMap.isEmpty()) {
            hintParts.add(formatTopTags(linkedContextTagCountMap, 3));
        }
        if (chapterPrefixCountMap != null && !chapterPrefixCountMap.isEmpty()) {
            hintParts.add(formatChapterPrefixSpread(chapterPrefixCountMap));
        }
        sb.append(String.join(" · ", hintParts));
        if (scaffoldStyle) {
            sb.append(msg("chat.ai.fallback.linked-context.suffix-scaffold"));
        } else {
            sb.append(msg("chat.ai.fallback.linked-context.suffix"));
        }
    }

    /**
     * Formats chapter Prefix counts for person-meaning sentences.
     */
    private String formatChapterPrefixSpread(final Map<String, Integer> chapterPrefixCountMap) {
        if (chapterPrefixCountMap == null || chapterPrefixCountMap.isEmpty()) return "";

        return chapterPrefixCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .map(entry -> entry.getKey() + "(" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    private static final int RULE_PRIMARY_EVIDENCE_MAX_LENGTH = PersonSnapshotService.RULE_PRIMARY_EVIDENCE_MAX_LENGTH;



    /**
     * RULE_PRIMARY 근거 장면에서 대화 인용·화자 라벨을 줄여 한 줄 요약으로 만듭니다.
     *
     * <p>hybrid SNAPSHOT용 스냅샷 스니펫 원문은 변경하지 않습니다.</p>
     */
    public String compactRulePrimaryEvidenceSnippet(final String snippet) {
        return compactRulePrimaryEvidenceSnippet(snippet, RULE_PRIMARY_EVIDENCE_MAX_LENGTH);
    }

    private String compactRulePrimaryEvidenceSnippet(final String snippet, final int maxLength) {
        if (StringUtils.isBlank(snippet)) return "";

        String normalized = StringUtils.normalizeSpace(snippet);
        normalized = normalized.replaceAll("\"[^\"]*\"", " ");
        normalized = normalized.replaceAll("[\\p{L}0-9]+님\\s*:", " ");
        normalized = normalized.replaceAll("\\b나\\s*:", " ");
        normalized = StringUtils.normalizeSpace(normalized);
        normalized = normalized.replaceAll("\\.{2,}", "...");
        if (StringUtils.isBlank(normalized)) {
            normalized = StringUtils.normalizeSpace(snippet.replaceAll("\"[^\"]*\"", " "));
        }
        return StringUtils.abbreviate(normalized, maxLength);
    }

    /**
     * RULE_PRIMARY fallback 답변에 근거 장면을 덧붙입니다.
     *
     * <p>질문 유형별 {@link PersonSnapshotService.PersonMeaningSnapshotOptions} 예산을 따릅니다. 태도 질문은 hybrid와 같이
     * 최대 {@link PersonSnapshotService}건·PersonSnapshotService stance snippet max자,
     * 그 외는 PersonSnapshotService meaning evidence limit건·{@link PersonSnapshotService#RULE_PRIMARY_EVIDENCE_MAX_LENGTH}자입니다.</p>
     */
    public void appendRulePrimaryEvidenceSection(final StringBuilder sb, final List<String> evidenceSnippets) {
        appendRulePrimaryEvidenceSection(sb, evidenceSnippets, PersonSnapshotService.PersonMeaningSnapshotOptions.defaults());
    }

    private void appendRulePrimaryEvidenceSection(
            final StringBuilder sb,
            final List<String> evidenceSnippets,
            final PersonSnapshotService.PersonMeaningSnapshotOptions options
    ) {
        if (evidenceSnippets == null || evidenceSnippets.isEmpty()) return;

        final String compact = evidenceSnippets.stream()
                .filter(StringUtils::isNotBlank)
                .limit(options.evidenceLimit())
                .map(snippet -> compactRulePrimaryEvidenceSnippet(snippet, options.snippetMaxLength()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" | "));
        if (StringUtils.isBlank(compact)) return;

        final String evidenceLabel = options.spreadEvidenceAcrossSources()
                ? msg("chat.ai.fallback.section.evidence-full")
                : msg("chat.ai.fallback.section.evidence-compact");
        sb.append("\n").append(evidenceLabel).append(": ").append(compact);
    }

    /**
     * person-meaning 해석 리드의 집계 본문(주어 없음)을 조립합니다.
     */
    private String buildPersonMeaningLeadBody(
            final Map<String, Integer> repeatedTagCountMap,
            final List<String> roleAxesKo,
            final Map<String, Integer> contentKindCountMap,
            final Map<String, Integer> linkedContextTagCountMap,
            final Map<String, Integer> chapterPrefixCountMap
    ) {
        final List<String> leadParts = new ArrayList<>();
        if (repeatedTagCountMap != null && !repeatedTagCountMap.isEmpty()) {
            leadParts.add(msg("chat.ai.fallback.lead-body.repeated-axis", formatTopTagsForDisplay(repeatedTagCountMap, 3)));
        }
        if (linkedContextTagCountMap != null && !linkedContextTagCountMap.isEmpty()) {
            leadParts.add(msg("chat.ai.fallback.lead-body.linked-tags", formatTopTagsForDisplay(linkedContextTagCountMap, 2)));
        }
        if (chapterPrefixCountMap != null && !chapterPrefixCountMap.isEmpty()) {
            leadParts.add(msg("chat.ai.fallback.lead-body.chapter", formatChapterPrefixSpread(chapterPrefixCountMap)));
        }
        if (roleAxesKo != null && !roleAxesKo.isEmpty()) {
            final String topRoleAxis = roleAxesKo.get(0);
            final int parenIndex = topRoleAxis.indexOf('(');
            final String roleLabel = parenIndex > 0 ? topRoleAxis.substring(0, parenIndex) : topRoleAxis;
            if (StringUtils.isNotBlank(roleLabel)) {
                leadParts.add(msg("chat.ai.fallback.lead-body.role-axis", roleLabel));
            }
        }

        final String contentKindHint = formatPersonMeaningContentKindHint(contentKindCountMap);
        if (StringUtils.isNotBlank(contentKindHint)) {
            leadParts.add(contentKindHint);
        }
        if (leadParts.isEmpty()) return "";
        return String.join(", ", leadParts);
    }

    /**
     * person-meaning 해석 리드 문단을 규칙 기반으로 조립합니다.
     */
    private String buildPersonMeaningInterpretiveLead(
            final String target,
            final Map<String, Integer> repeatedTagCountMap,
            final List<String> roleAxesKo,
            final Map<String, Integer> contentKindCountMap,
            final Map<String, Integer> linkedContextTagCountMap,
            final Map<String, Integer> chapterPrefixCountMap
    ) {
        if (StringUtils.isBlank(target)) return "";

        final String leadBody = buildPersonMeaningLeadBody(
                repeatedTagCountMap,
                roleAxesKo,
                contentKindCountMap,
                linkedContextTagCountMap,
                chapterPrefixCountMap
        );
        if (StringUtils.isBlank(leadBody)) return "";
        return msg("chat.ai.lead.person-meaning", target, leadBody);
    }

    /**
     * person-stance 해석 리드 문단을 규칙 기반으로 조립합니다.
     *
     * <p>person-meaning과 동일한 집계 본문을 쓰되, 3인칭 주어 없이 2인칭 태도 비춤으로 시작합니다.</p>
     */
    private String buildPersonStanceInterpretiveLead(
            final String target,
            final Map<String, Integer> repeatedTagCountMap,
            final List<String> roleAxesKo,
            final Map<String, Integer> contentKindCountMap,
            final Map<String, Integer> linkedContextTagCountMap,
            final Map<String, Integer> chapterPrefixCountMap
    ) {
        final String leadBody = buildPersonMeaningLeadBody(
                repeatedTagCountMap,
                roleAxesKo,
                contentKindCountMap,
                linkedContextTagCountMap,
                chapterPrefixCountMap
        );
        if (StringUtils.isBlank(leadBody)) return "";
        return msg("chat.ai.lead.person-stance", leadBody);
    }

    /**
     * DREAM/DIARY/NOTE 비중을 person-meaning 리드 문장으로 변환합니다.
     */
    private String formatPersonMeaningContentKindHint(final Map<String, Integer> contentKindCountMap) {
        if (contentKindCountMap == null || contentKindCountMap.isEmpty()) return "";

        final int dreamCount = contentKindCountMap.getOrDefault("DREAM", 0);
        final int diaryCount = contentKindCountMap.getOrDefault("DIARY", 0);
        final int noteCount = contentKindCountMap.getOrDefault("NOTE", 0);
        if (dreamCount == 0 && diaryCount == 0 && noteCount == 0) return "";

        if (dreamCount > diaryCount && dreamCount > noteCount) return msg("chat.ai.hint.content-kind.dream");
        if (diaryCount > dreamCount && diaryCount > noteCount) return msg("chat.ai.hint.content-kind.diary");
        if (noteCount > dreamCount && noteCount > diaryCount) return msg("chat.ai.hint.content-kind.note");
        if (dreamCount == diaryCount && dreamCount > 0) return msg("chat.ai.hint.content-kind.dream-diary");
        return "";
    }

    /**
     * 모델이 빈 분류만 내놓았을 때 스캐폴드 데이터로 결정적 person-meaning 답변을 만듭니다.
     */
    /**
     * 모델이 빈 분류만 내놓았을 때 스캐폴드 데이터로 결정적 person-meaning 답변을 만듭니다.
     */
    public String buildPersonMeaningDeterministicFallback(
            final PersonFocus personFocus,
            final List<RagSearchResult> results
    ) {
        if (personFocus == null) {
            return msg("chat.ai.clarify.need-context");
        }
        if (results == null || results.isEmpty()) {
            final String target = personFocusResolver.resolvePersonFocusTarget(personFocus);
            return msg("chat.ai.clarify.no-info-for-target", target);
        }

        final String target = personFocusResolver.resolvePersonFocusTarget(personFocus);
        final PersonMeaningSnapshot snapshot = personSnapshotService.build(results, personFocus, null);
        final StringBuilder sb = new StringBuilder();

        final String interpretiveLead = buildPersonMeaningInterpretiveLead(
                target,
                snapshot.repeatedTagCountMap(),
                snapshot.roleAxesKo(),
                snapshot.contentKindCountMap(),
                snapshot.linkedContextTagCountMap(),
                snapshot.chapterPrefixCountMap()
        );
        if (StringUtils.isNotBlank(interpretiveLead)) {
            sb.append(interpretiveLead).append("\n\n");
        } else {
            sb.append(msg("chat.ai.fallback.person-meaning.intro-no-lead", target)).append("\n\n");
        }

        if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append(msg("chat.ai.fallback.section.repeated-axis")).append(' ')
                    .append(formatTopTagsForDisplay(snapshot.repeatedTagCountMap(), 6)).append('\n');
            sb.append(msg("chat.ai.fallback.person-meaning.repeated-axis-interpret", target)).append('\n');
        } else {
            sb.append(msg("chat.ai.fallback.person-meaning.repeated-axis-empty", target)).append('\n');
        }

        if (!snapshot.linkedContextTagCountMap().isEmpty()) {
            sb.append(msg("chat.ai.fallback.section.linked-context")).append(' ')
                    .append(formatTopTagsForDisplay(snapshot.linkedContextTagCountMap(), 8))
                    .append('\n');
            sb.append(msg("chat.ai.fallback.person-meaning.linked-context-interpret", target)).append('\n');
        }

        if (!snapshot.chapterPrefixCountMap().isEmpty()) {
            sb.append(msg("chat.ai.fallback.section.chapter-prefix")).append(' ')
                    .append(formatChapterPrefixSpread(snapshot.chapterPrefixCountMap()))
                    .append('\n');
        }

        sb.append(msg("chat.ai.fallback.section.role-function")).append(' ');
        if (!snapshot.roleAxesKo().isEmpty()) {
            sb.append(String.join(", ", snapshot.roleAxesKo())).append('\n');
        } else {
            appendPersonMeaningLinkedContextRoleHint(
                    sb,
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterPrefixCountMap(),
                    false
            );
            sb.append('\n');
        }

        if (!snapshot.contentKindCountMap().isEmpty()) {
            sb.append(msg("chat.ai.fallback.section.content-kind")).append(' ')
                    .append(formatContentKindSpread(snapshot.contentKindCountMap())).append('\n');
        }

        if (StringUtils.isNotBlank(snapshot.firstDate()) || StringUtils.isNotBlank(snapshot.lastDate())) {
            sb.append(msg(
                    "chat.ai.fallback.section.period",
                    StringUtils.defaultIfBlank(snapshot.firstDate(), "?"),
                    StringUtils.defaultIfBlank(snapshot.lastDate(), "?")
            )).append('\n');
        }

        appendRulePrimaryEvidenceSection(sb, snapshot.evidenceSnippets());

        sb.append('\n').append(msg("chat.ai.fallback.person-meaning.uncertainty"));
        return sb.toString().trim();
    }

    /**
     * 대화/기록 속 인물 등장 방식 질문용 규칙 기반 응답을 만듭니다.
     */
    /**
     * 대화/기록 속 인물 등장 방식 질문용 규칙 기반 응답을 만듭니다.
     */
    public String buildPersonAppearanceDeterministicFallback(
            final PersonFocus personFocus,
            final List<RagSearchResult> results
    ) {
        if (personFocus == null) {
            return msg("chat.ai.clarify.need-context");
        }
        if (results == null || results.isEmpty()) {
            final String target = personFocusResolver.resolvePersonFocusTarget(personFocus);
            return msg("chat.ai.clarify.no-appearance-for-target", target);
        }

        final String target = personFocusResolver.resolvePersonFocusTarget(personFocus);
        final PersonMeaningSnapshot snapshot = personSnapshotService.build(results, personFocus, null);
        final StringBuilder sb = new StringBuilder();

        final String interpretiveLead = buildPersonAppearanceInterpretiveLead(
                target,
                snapshot.repeatedTagCountMap(),
                snapshot.roleAxesKo(),
                snapshot.contentKindCountMap(),
                snapshot.linkedContextTagCountMap(),
                snapshot.chapterPrefixCountMap()
        );
        if (StringUtils.isNotBlank(interpretiveLead)) {
            sb.append(interpretiveLead).append("\n\n");
        } else {
            sb.append(msg("chat.ai.fallback.person-appearance.intro-no-lead", target)).append("\n\n");
        }

        sb.append(msg("chat.ai.fallback.person-appearance.section-tone")).append(' ');
        if (!snapshot.roleAxesKo().isEmpty()) {
            sb.append(String.join(", ", snapshot.roleAxesKo()));
        } else if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append(msg("chat.ai.fallback.person-appearance.repeated-tags-prefix")).append(' ')
                    .append(formatTopTagsForDisplay(snapshot.repeatedTagCountMap(), 6));
        } else {
            sb.append(msg("chat.ai.fallback.person-appearance.insufficient-axis"));
        }
        sb.append('\n');

        sb.append(msg("chat.ai.fallback.person-appearance.section-repeat")).append(' ');
        boolean hasRepeatPattern = false;
        if (!snapshot.linkedContextTagCountMap().isEmpty()) {
            sb.append(msg("chat.ai.fallback.person-appearance.linked-context-prefix")).append(' ')
                    .append(formatTopTagsForDisplay(snapshot.linkedContextTagCountMap(), 6));
            hasRepeatPattern = true;
        }
        if (!snapshot.chapterPrefixCountMap().isEmpty()) {
            if (hasRepeatPattern) sb.append("; ");
            sb.append(msg("chat.ai.fallback.person-appearance.chapter-prefix")).append(' ')
                    .append(formatChapterPrefixSpread(snapshot.chapterPrefixCountMap()));
            hasRepeatPattern = true;
        }
        if (!snapshot.contentKindCountMap().isEmpty()) {
            if (hasRepeatPattern) sb.append("; ");
            sb.append(msg("chat.ai.fallback.person-appearance.content-kind-prefix")).append(' ')
                    .append(formatContentKindSpread(snapshot.contentKindCountMap()));
            hasRepeatPattern = true;
        }
        if (StringUtils.isNotBlank(snapshot.firstDate()) || StringUtils.isNotBlank(snapshot.lastDate())) {
            if (hasRepeatPattern) sb.append("; ");
            sb.append(msg("chat.ai.fallback.person-appearance.period-prefix")).append(' ')
                    .append(StringUtils.defaultIfBlank(snapshot.firstDate(), "?"))
                    .append(" ~ ")
                    .append(StringUtils.defaultIfBlank(snapshot.lastDate(), "?"));
            hasRepeatPattern = true;
        }
        if (!hasRepeatPattern) {
            sb.append(msg("chat.ai.fallback.person-appearance.insufficient-repeat"));
        }
        sb.append('\n');

        sb.append(msg("chat.ai.fallback.person-appearance.section-bundled")).append(' ');
        if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append(formatTopTagsForDisplay(snapshot.repeatedTagCountMap(), 6));
        } else {
            sb.append(msg("chat.ai.fallback.person-appearance.no-person-tags"));
        }
        sb.append('\n');

        sb.append(msg("chat.ai.fallback.person-appearance.uncertainty")).append('\n');

        appendRulePrimaryEvidenceSection(sb, snapshot.evidenceSnippets());

        return sb.toString().trim();
    }

    /**
     * person-appearance 해석 리드 문단을 규칙 기반으로 조립합니다.
     */
    private String buildPersonAppearanceInterpretiveLead(
            final String target,
            final Map<String, Integer> repeatedTagCountMap,
            final List<String> roleAxesKo,
            final Map<String, Integer> contentKindCountMap,
            final Map<String, Integer> linkedContextTagCountMap,
            final Map<String, Integer> chapterPrefixCountMap
    ) {
        if (StringUtils.isBlank(target)) return "";

        final String leadBody = buildPersonMeaningLeadBody(
                repeatedTagCountMap,
                roleAxesKo,
                contentKindCountMap,
                linkedContextTagCountMap,
                chapterPrefixCountMap
        );
        if (StringUtils.isBlank(leadBody)) return "";
        return msg("chat.ai.lead.person-appearance", target, leadBody);
    }
    /**
     * 태도 질문에서 모델이 성격 평가·코칭만 내놓았을 때 기록 근거로 2인칭 태도 답변을 만듭니다.
     */
    /**
     * 태도 질문에서 모델이 성격 평가·코칭만 내놓았을 때 기록 근거로 2인칭 태도 답변을 만듭니다.
     */
    public String buildPersonStanceDeterministicFallback(
            final PersonFocus personFocus,
            final List<RagSearchResult> results,
            final String queryText
    ) {
        if (personFocus == null) {
            return msg("chat.ai.clarify.need-context");
        }
        if (results == null || results.isEmpty()) {
            final String target = personFocusResolver.resolvePersonFocusTarget(personFocus);
            return msg("chat.ai.clarify.no-stance-for-target", target);
        }

        final String target = personFocusResolver.resolvePersonFocusTarget(personFocus);
        final PersonMeaningSnapshot snapshot = personSnapshotService.build(results, personFocus, queryText);
        final StringBuilder sb = new StringBuilder();

        final String interpretiveLead = buildPersonStanceInterpretiveLead(
                target,
                snapshot.repeatedTagCountMap(),
                snapshot.roleAxesKo(),
                snapshot.contentKindCountMap(),
                snapshot.linkedContextTagCountMap(),
                snapshot.chapterPrefixCountMap()
        );
        if (StringUtils.isNotBlank(interpretiveLead)) {
            sb.append(interpretiveLead);
        } else {
            sb.append(msg("chat.ai.fallback.person-stance.intro-no-lead", target));
        }
        sb.append(msg("chat.ai.fallback.person-stance.caveat"));

        final List<String> stanceSnippets = snapshot.evidenceSnippets();
        if (stanceSnippets != null && !stanceSnippets.isEmpty()) {
            final int snippetLimit = Math.min(stanceSnippets.size(), 3);
            sb.append(msg("chat.ai.fallback.person-stance.evidence-intro"))
                    .append(String.join(" / ", stanceSnippets.subList(0, snippetLimit)));
        }

        return sb.toString().trim();
    }

    /**
     * DREAM/DIARY/NOTE 비율을 person-meaning fallback 문장으로 포맷합니다.
     */
    private String formatContentKindSpread(final Map<String, Integer> contentKindCountMap) {
        if (contentKindCountMap == null || contentKindCountMap.isEmpty()) return "";

        final List<String> parts = new ArrayList<>();
        appendContentKindPart(parts, contentKindCountMap, "DREAM", msg("chat.ai.fallback.content-kind.dream"));
        appendContentKindPart(parts, contentKindCountMap, "DIARY", msg("chat.ai.fallback.content-kind.diary"));
        appendContentKindPart(parts, contentKindCountMap, "NOTE", msg("chat.ai.fallback.content-kind.note"));
        for (final Map.Entry<String, Integer> entry : contentKindCountMap.entrySet()) {
            if (entry == null || entry.getValue() == null || entry.getValue() <= 0) continue;
            final String kind = StringUtils.defaultString(entry.getKey());
            if ("DREAM".equalsIgnoreCase(kind) || "DIARY".equalsIgnoreCase(kind) || "NOTE".equalsIgnoreCase(kind)) {
                continue;
            }
            parts.add(kind + "(" + entry.getValue() + ")");
        }
        return String.join(", ", parts);
    }

    /**
     * 기록 유형 한 항목을 fallback 문장 조각으로 추가합니다.
     */
    private void appendContentKindPart(
            final List<String> parts,
            final Map<String, Integer> contentKindCountMap,
            final String kind,
            final String label
    ) {
        for (final Map.Entry<String, Integer> entry : contentKindCountMap.entrySet()) {
            if (entry == null || entry.getValue() == null || entry.getValue() <= 0) continue;
            if (!kind.equalsIgnoreCase(StringUtils.defaultString(entry.getKey()))) continue;
            parts.add(label + "(" + entry.getValue() + ")");
            return;
        }
    }
}
