package io.nicheblog.dreamdiary.feature.chat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatSessionEntity;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.chat.model.RagIntent;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingSearchService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * ChatAIService
 * <pre>
 *  사용자 메시지 저장, 대화 맥락 구성, AI 응답 생성, WebSocket 브로드캐스트를 묶어 처리하는 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatAIService {

    /** RAG 검색에서 가져올 최대 저널 엔트리 수 */
    private static final int RAG_TOP_K = 5;
    /** 요약형 RAG 검색에서 가져올 최대 저널 엔트리 수 */
    private static final int RAG_SUMMARY_TOP_K = 12;
    /** 통섭형 RAG 검색에서 가져올 최대 저널 엔트리 수 */
    private static final int RAG_SYNTHESIS_TOP_K = 25;
    /** 관련 없는 기록을 억지로 주입하지 않기 위한 최소 검색 점수 */
    private static final double RAG_MIN_SCORE = 0.35D;
    /** 통섭형 질문에서 더 넓은 맥락을 가져오기 위한 최소 검색 점수 */
    private static final double RAG_SYNTHESIS_MIN_SCORE = 0.25D;
    /** RAG 컨텍스트에 포함할 엔트리당 최대 텍스트 길이 */
    private static final int RAG_TEXT_MAX_LENGTH = 300;
    /** 통섭형 RAG 컨텍스트에 포함할 엔트리당 최대 스니펫 길이 */
    private static final int RAG_SYNTHESIS_TEXT_MAX_LENGTH = 220;
    /** person-meaning 질문에서 직접 언급 source에 허용할 스니펫 길이 */
    private static final int RAG_PERSON_FOCUS_SNIPPET_MAX_LENGTH = 400;
    /** entity catalog 연결 기록을 RAG에 강제 포함할 최대 건수 */
    private static final int RAG_ENTITY_LINK_MAX = 10;
    /** entity catalog 역할 축을 한국어 해석 라벨로 변환 */
    private static final Map<JournalEntityRoleType, String> PERSON_ROLE_AXIS_LABELS = Map.ofEntries(
            Map.entry(JournalEntityRoleType.COLLABORATION, "\uD611\uC5C5\u00B7\uB3D9\uD589 \uCD95"),
            Map.entry(JournalEntityRoleType.TENSION, "\uAE34\uC7A5\u00B7\uACBD\uACC4 \uCD95"),
            Map.entry(JournalEntityRoleType.EVALUATION, "\uD3C9\uAC00\u00B7\uC778\uC815 \uCD95"),
            Map.entry(JournalEntityRoleType.CARE, "\uC704\uB85C\u00B7\uBCF4\uD638 \uCD95"),
            Map.entry(JournalEntityRoleType.CONFLICT, "\uAC08\uB4F1\u00B7\uB300\uB9BD \uCD95"),
            Map.entry(JournalEntityRoleType.DESIRE, "\uC6D0\uD568\u00B7\uB04C\uC784 \uCD95"),
            Map.entry(JournalEntityRoleType.SYMBOLIC_FIGURE, "\uC0C1\uC9D5\u00B7\uB300\uC0C1 \uCD95"),
            Map.entry(JournalEntityRoleType.UNKNOWN, "\uBBF8\uBD84\uB958 \uCD95")
    );
    /** ?몃Ъ focus ?좏겙 理쒖냼 湲몄씠 */
    private static final int PERSON_FOCUS_MIN_TOKEN_LENGTH = 2;
    /** ?몃Ъ ?의? 吏덈Ц ?먯꽌 person focus瑜? ?명똿?섎뒗 臾몄옣 ?ы듃 */
    private static final String[] PERSON_FOCUS_HINTS = {
            "\uB0B4 \uAE30\uB85D", "\uAE30\uB85D\uC5D0\uC11C",
            "\uC5B4\uB5A4 \uC758\uBBF8", "\uBB34\uC2A8 \uC758\uBBF8",
            "\uC5B4\uB5A4 \uC874\uC7AC", "\uC5B4\uB5A4 \uC5ED\uD560",
            "\uC65C \uBC18\uBCF5", "\uC65C \uC790\uC8FC",
            "\uC5B4\uB5BB\uAC8C \uB4F1\uC7A5", "\uB4F1\uC7A5\uD558\uB294"
    };
    /** person focus ?좏겙 異붿텧 ???쒖쇅??遺덉슜??*/
    private static final Set<String> PERSON_FOCUS_STOPWORDS = Set.of(
            "\uB098\uB294", "\uB108\uB294", "\uB0B4", "\uB098", "\uAE30\uB85D",
            "dreamdiary", "Dreamdiary", "AI",
            "\uC758\uBBF8", "\uB4F1\uC7A5", "\uB4F1\uC7A5\uD574", "\uB4F1\uC7A5\uD558\uB294",
            "\uBB34\uC2A8", "\uC5B4\uB5A4", "\uC5B4\uB5BB\uAC8C",
            "\uC5ED\uD560", "\uC874\uC7AC", "\uBC18\uBCF5", "\uC790\uC8FC",
            "\uD1B5\uC12D", "\uD574\uC11D", "\uC694\uC57D", "\uC815\uB9AC",
            "\uB9D0\uD574\uC918", "\uB9D0\uD574", "\uBCF4\uC5EC\uC918",
            "\uB300\uD574", "\uAD00\uB828", "\uC804\uCCB4", "\uB9E5\uB77D"
    );
    /** 세션별 프롬프트보다 우선 적용할 응답 안전 규칙 */
    private static final String RESPONSE_GUARD_PROMPT = String.join("\n",
            "",
            "## 응답 규칙",
            "반드시 한국어로만 답변한다. 사용자가 명시적으로 요청하지 않는 한 중국어, 영어, 일본어 문장을 섞지 않는다.",
            "사용자의 질문이 한국어이면 자연스러운 한국어 구어체로 답한다.",
            "참고할 저널 기록은 관련성이 충분할 때만 사용한다. 관련이 약하면 억지로 연결하지 않는다.",
            "사용자가 인물, 장소, 태그, 고유명사를 물으면 외부 상식보다 먼저 참고 저널 기록 안의 맥락으로 이해한다.",
            "인물의 직업, 역할, 관계 지위는 참고 기록에 직접 드러난 표현이 있을 때만 말한다. 근거가 없으면 추정하지 말고 확인되지 않는다고 밝힌다.",
            "인물/상징의 의미를 묻는 질문에는 최소한 등장 장면, 반복되는 관계 축, 정서적 기능, 확실하지 않은 점을 구분해 답한다.",
            "기록에서 확인되지 않는 사람, 사건, 사실은 아는 척하지 말고 확인되는 정보가 없다고 말한 뒤 필요한 맥락을 짧게 물어본다.",
            "참고 기록과 질문이 맞지 않더라도 '이전 기록과 무관합니다' 같은 시스템 내부 판단을 길게 설명하지 않는다."
    );
    /** LLM이 언어 규칙을 어긴 경우 1회 재시도할 때 추가하는 지시문 */
    private static final String LANGUAGE_RETRY_PROMPT = String.join("\n",
            "",
            "## 중요",
            "직전 응답에는 허용되지 않는 중국어/한자 문장이 섞였습니다.",
            "이번 응답은 반드시 한글 중심의 한국어 문장으로만 다시 작성하세요.",
            "중국어 원문, 한자 이름 추정, 병음, 번체/간체 문자를 쓰지 마세요."
    );

    /** person-meaning 답변에 내부 스캐폴드/메타 필드명이 새어 나오면 degraded로 본다. */
    private static final String[] PERSON_MEANING_SCAFFOLD_LEAK_MARKERS = {
            "role_axes_ko", "roleaxesko", "repeated_tags", "PERSON_MEANING_SCAFFOLD",
            "1_\uBC18\uBCF5\uCD95", "2_\uC5ED\uD560\uAE30\uB2A5", "3_\uAE30\uB85D\uC720\uD615",
            "4_\uADFC\uAC70\uC7A5\uBA74\uD78C\uD2B8", "5_\uD655\uC815\uBD88\uAC00", "entity catalog"
    };
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ChatSettingService chatSettingService;
    private final OllamaClient ollamaClient;
    private final JournalEntryEmbeddingSearchService embeddingSearchService;
    private final JournalEntityFocusService journalEntityFocusService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 세션별 응답 취소 플래그 */
    private final Map<Integer, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    /**
     * 사용자 메시지를 저장한 뒤 최근 대화 맥락을 포함해 AI 응답을 생성하고 세션 구독자에게 전송한다.
     *
     * @param sessionId 메시지가 속한 채팅 세션 ID
     * @param message 사용자 입력 메시지
     * @throws Exception 세션 검증, 메시지 저장, AI 호출 중 예외가 발생한 경우
     */
    public void processChat(final Integer sessionId, final String message) throws Exception {
        cancelFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false)).set(false);

        final ChatSessionEntity session = chatSessionService.getMySessionEntity(sessionId);

        // 1. 사용자 메시지 저장
        final ChatMessageDto userMessage = ChatMessageDto.builder()
                        .sessionId(sessionId)
                        .seq(chatMessageService.getNextSeq(sessionId))
                        .role("USER")
                        .content(message)
                        .build();
        final ServiceResponse userResult = chatMessageService.regist(userMessage);
        chatSessionService.touchAfterMessage(sessionId, message);

        // 2. 사용자 메시지 broadcast
        messagingTemplate.convertAndSend(
                "/topic/chat/session/" + sessionId,
                AjaxResponse.fromResponseWithObj(
                        userResult,
                        MessageUtils.RSLT_SUCCESS
                )
        );

        // 3. AI 응답 생성 (RAG 컨텍스트 주입)
        final int recentMessageLimit = chatSettingService.getMyRecentMessageLimit();
        final RagContext ragContext = buildRagContext(message);
        final String systemPrompt = buildSystemPromptWithRag(
                StringUtils.defaultIfBlank(session.getSystemPrompt(), chatSessionService.getDefaultSystemPrompt()),
                ragContext
        );
        final List<ChatMessageDto> contextMessages = sanitizeContextMessages(
                chatMessageService.getRecentContextMessages(sessionId, recentMessageLimit)
        );
        String rawResponse = ollamaClient.chat(
                        systemPrompt,
                        contextMessages
                );
        if (containsDisallowedHanScript(rawResponse)) {
            log.warn("AI response language guard retry. sessionId={}", sessionId);
            rawResponse = ollamaClient.chat(systemPrompt + LANGUAGE_RETRY_PROMPT, contextMessages);
        }

        // 취소 요청이 들어왔으면 저장/broadcast 없이 종료
        if (isCancelled(sessionId)) {
            log.info("AI response cancelled. sessionId={}", sessionId);
            cancelFlags.remove(sessionId);
            return;
        }

        String strippedResponse = stripMarkdown(rawResponse);
        String responseMode = "LLM";
        if (containsDisallowedHanScript(strippedResponse)) {
            strippedResponse = buildLanguageFallback(message, ragContext);
            responseMode = "LANGUAGE_FALLBACK";
        } else if (isHollowPersonMeaningResponse(strippedResponse, ragContext)) {
            log.warn("AI person-meaning degraded response, scaffold fallback applied. sessionId={}", sessionId);
            strippedResponse = buildPersonMeaningDeterministicFallback(ragContext);
            responseMode = "PERSON_MEANING_FALLBACK";
        }
        final String aiResponse = strippedResponse;

        // 4. AI 메시지 저장
        final ChatMessageDto aiMessage = ChatMessageDto.builder()
                        .sessionId(sessionId)
                        .seq(chatMessageService.getNextSeq(sessionId))
                        .role("ASSISTANT")
                        .title("Dreamdiary AI")
                        .content(aiResponse)
                        .metadataJson(buildRagMetadataJson(ragContext, responseMode))
                        .build();
        final ServiceResponse aiResult = chatMessageService.regist(aiMessage);
        chatSessionService.touchAfterMessage(sessionId, message);

        // 5. AI 메시지 broadcast
        messagingTemplate.convertAndSend(
                "/topic/chat/session/" + sessionId,
                AjaxResponse.fromResponseWithObj(
                        aiResult,
                        MessageUtils.RSLT_SUCCESS
                )
        );

        cancelFlags.remove(sessionId);
    }

    /**
     * 세션의 AI 응답 생성을 취소 요청한다.
     *
     * <p>Ollama 호출이 완료되기 전에 플래그를 세팅하면,
     * 응답이 도착해도 저장과 broadcast를 건너뛴다.</p>
     *
     * @param sessionId 취소할 채팅 세션 ID
     */
    public void cancelChat(final Integer sessionId) {
        if (sessionId == null) return;
        cancelFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false)).set(true);
        log.info("AI response cancel requested. sessionId={}", sessionId);
    }

    /**
     * 취소 플래그가 세팅되어 있는지 확인한다.
     */
    private boolean isCancelled(final Integer sessionId) {
        final AtomicBoolean flag = cancelFlags.get(sessionId);
        return flag != null && flag.get();
    }

    /**
     * 사용자 메시지와 의미상 유사한 저널 기록을 검색해 RAG 컨텍스트 문자열을 생성합니다.
     *
     * <p>Ollama 임베딩 호출 실패 시 null을 반환해 채팅이 중단되지 않도록 합니다.</p>
     *
     * @param queryText 검색할 사용자 메시지
     * @return 포맷팅된 RAG 컨텍스트 문자열, 결과가 없거나 오류 발생 시 null
     */
    private RagContext buildRagContext(final String queryText) {
        final RagIntent intent = detectRagIntent(queryText);
        try {
            if (intent == RagIntent.SYNTHESIS && isPersonMeaningQuery(queryText)) {
                return buildPersonMeaningTagOnlyRagContext(queryText, intent);
            }

            final int topK = resolveRagTopK(intent);
            final double minScore = resolveRagMinScore(intent);
            final List<RagSearchResult> keywordResults = embeddingSearchService.searchByKeywordWithScore(queryText, topK);
            final List<RagSearchResult> vectorResults = embeddingSearchService.searchWithScore(queryText, topK, minScore);
            List<RagSearchResult> mergedResults = mergeRagResults(keywordResults, vectorResults, topK);
            PersonFocus personFocus = resolvePersonFocus(queryText, intent, mergedResults);
            mergedResults = mergePersonTagResults(queryText, intent, mergedResults, personFocus, topK);
            personFocus = resolvePersonFocus(queryText, intent, mergedResults);
            final List<RagSearchResult> entityBoosted = mergeEntityLinkedResults(mergedResults, personFocus, topK);
            final List<RagSearchResult> results = prioritizeResultsForPersonFocus(entityBoosted, personFocus);
            if (results.isEmpty() && personFocus == null) return RagContext.empty(intent);
            log.info("AI RAG context built. intent={}, queryLength={}, keywordCount={}, vectorCount={}, mergedCount={}",
                    intent, StringUtils.length(queryText), keywordResults.size(), vectorResults.size(), results.size());
            if (personFocus != null) {
                log.info("AI RAG person focus applied. target={}, aliases={}, matchedSourceCount={}",
                        personFocus.primaryToken(), personFocus.tokens(), personFocus.matchedSourceCount());
            }
            logRagSources(results);

            return new RagContext(intent, results, buildRagContextText(intent, results, personFocus), personFocus);
        } catch (final Exception e) {
            log.warn("RAG context search failed, proceeding without context. intent={}, error={}", intent, e.getMessage());
            return RagContext.empty(intent);
        }
    }

    /**
     * person-meaning SYNTHESIS questions use only journal entries whose payload tags match person focus tokens.
     *
     * <p>Entity catalog resolves alias tokens (for example {@code 원빈} -> {@code 김원빈}) but does not inject
     * catalog-linked entries, keyword hits, vector hits, or body-mention fallbacks.</p>
     */
    private RagContext buildPersonMeaningTagOnlyRagContext(final String queryText, final RagIntent intent) {
        final int topK = resolveRagTopK(intent);
        final List<String> queryTokens = extractPersonFocusTokens(queryText);
        if (queryTokens.isEmpty()) {
            log.info("AI RAG person-meaning tag-only skipped. reason=noPersonToken");
            return RagContext.empty(intent);
        }

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                journalEntityFocusService == null
                        ? null
                        : journalEntityFocusService.resolvePersonFocusSummary(queryTokens).orElse(null);
        final List<String> focusTokens = mergePersonFocusTokens(queryTokens, entitySummary);
        final List<RagSearchResult> tagResults = embeddingSearchService.searchByPersonTagsWithScore(focusTokens, topK);
        final PersonFocus personFocus = new PersonFocus(
                resolvePersonFocusPrimaryToken(queryTokens, entitySummary),
                focusTokens,
                tagResults.size(),
                null
        );

        if (tagResults.isEmpty()) {
            log.info("AI RAG person-meaning tag-only empty. target={}, tokens={}",
                    personFocus.primaryToken(), focusTokens);
            return new RagContext(
                    intent,
                    List.of(),
                    buildPersonMeaningTagOnlyEmptyContextText(personFocus),
                    personFocus
            );
        }

        log.info("AI RAG person-meaning tag-only built. target={}, tokens={}, tagCount={}",
                personFocus.primaryToken(), focusTokens, tagResults.size());
        logRagSources(tagResults);
        return new RagContext(
                intent,
                tagResults,
                buildRagContextText(intent, tagResults, personFocus),
                personFocus
        );
    }

    /**
     * person-meaning tag-only retrieval found zero tagged sources.
     */
    private String buildPersonMeaningTagOnlyEmptyContextText(final PersonFocus personFocus) {
        final StringBuilder sb = new StringBuilder();
        sb.append("RAG_INTENT: SYNTHESIS\n");
        sb.append("PERSON_MEANING_TAG_ONLY: true\n");
        sb.append("해당 인물 태그가 붙은 저널 기록이 없습니다. 본문에 이름만 등장한 기록은 person-meaning 해석 재료로 사용하지 않습니다.\n\n");
        appendPersonFocusBlock(sb, personFocus, List.of());
        return sb.toString().trim();
    }

    /**
     * 기본 시스템 프롬프트에 RAG 컨텍스트를 추가합니다.
     *
     * @param basePrompt 세션 또는 기본 시스템 프롬프트
     * @param ragContext 저널 검색 결과 컨텍스트
     * @return RAG 컨텍스트가 포함된 최종 시스템 프롬프트
     */
    private String buildSystemPromptWithRag(final String basePrompt, final RagContext ragContext) {
        final String guardedPrompt = StringUtils.defaultString(basePrompt) + RESPONSE_GUARD_PROMPT;
        if (ragContext == null || StringUtils.isBlank(ragContext.text())) return guardedPrompt;
        return guardedPrompt
                + "\n\n## 참고할 저널 기록\n"
                + "아래는 현재 질문과 관련성이 충분하다고 검색된 나의 저널 기록입니다.\n"
                + "질문 속 인물/키워드가 아래 기록에 등장하면, 외부 인물이 아니라 나의 Dreamdiary 기록 속 맥락으로 해석하세요.\n"
                + buildIntentPrompt(ragContext.intent())
                + "\n\n"
                + ragContext.text();
    }

    /**
     * 직접 키워드 검색 결과를 우선하고, 벡터 검색 결과를 뒤에 합칩니다.
     */
    private List<RagSearchResult> mergeRagResults(
            final List<RagSearchResult> keywordResults,
            final List<RagSearchResult> vectorResults,
            final int topK
    ) {
        final Map<Integer, RagSearchResult> merged = new LinkedHashMap<>();
        for (final RagSearchResult result : keywordResults) {
            if (result == null || result.getJournalEntryId() == null) continue;
            merged.put(result.getJournalEntryId(), result);
            if (merged.size() >= topK) return new ArrayList<>(merged.values());
        }
        for (final RagSearchResult result : vectorResults) {
            if (result == null || result.getJournalEntryId() == null) continue;
            merged.putIfAbsent(result.getJournalEntryId(), result);
            if (merged.size() >= topK) break;
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * person-meaning 질문에서는 person 태그 매칭 결과를 RAG 앞쪽에 우선 병합합니다.
     */
    private List<RagSearchResult> mergePersonTagResults(
            final String queryText,
            final RagIntent intent,
            final List<RagSearchResult> results,
            final PersonFocus personFocus,
            final int topK
    ) {
        if (intent != RagIntent.SYNTHESIS || !isPersonMeaningQuery(queryText)) {
            return results;
        }

        final List<String> tagTokens = personFocus != null && !personFocus.tokens().isEmpty()
                ? personFocus.tokens()
                : extractPersonFocusTokens(queryText);
        if (tagTokens.isEmpty()) return results;

        final List<RagSearchResult> tagResults = embeddingSearchService.searchByPersonTagsWithScore(tagTokens, topK);
        if (tagResults.isEmpty()) return results;

        log.info("AI RAG person-tag search applied. tokens={}, hitCount={}", tagTokens, tagResults.size());
        return mergeTagFirstResults(tagResults, results, topK);
    }

    /**
     * TAG 매칭 결과를 최우선으로 두고 나머지 RAG 결과를 뒤에 병합합니다.
     */
    private List<RagSearchResult> mergeTagFirstResults(
            final List<RagSearchResult> tagResults,
            final List<RagSearchResult> results,
            final int topK
    ) {
        final Map<Integer, RagSearchResult> merged = new LinkedHashMap<>();
        if (tagResults != null) {
            for (final RagSearchResult result : tagResults) {
                if (result == null || result.getJournalEntryId() == null) continue;
                merged.put(result.getJournalEntryId(), result);
            }
        }
        if (results != null) {
            for (final RagSearchResult result : results) {
                if (result == null || result.getJournalEntryId() == null) continue;
                merged.putIfAbsent(result.getJournalEntryId(), result);
            }
        }
        return merged.values().stream().limit(topK).collect(Collectors.toList());
    }

    /**
     * entity catalog에 연결된 저널 엔트리를 RAG 결과 앞에 강제 병합합니다.
     *
     * <p>person-meaning 질문에서 벡터 점수가 낮아 빠진 직접 연결 기록을 보강합니다.</p>
     */
    private List<RagSearchResult> mergeEntityLinkedResults(
            final List<RagSearchResult> results,
            final PersonFocus personFocus,
            final int topK
    ) {
        if (personFocus == null || personFocus.entitySummary() == null || results == null) {
            return results;
        }

        final List<Integer> entryIds = personFocus.entitySummary().journalEntryIds();
        if (entryIds == null || entryIds.isEmpty()) {
            return results;
        }

        final List<Integer> limitedEntryIds = entryIds.stream()
                .limit(RAG_ENTITY_LINK_MAX)
                .collect(Collectors.toList());
        final List<RagSearchResult> entityLinked = embeddingSearchService.findByJournalEntryIds(limitedEntryIds);
        if (entityLinked.isEmpty()) {
            return results;
        }

        log.info(
                "AI RAG entity-linked boost applied. target={}, catalogEntryCount={}, injectedCount={}",
                resolvePersonFocusTarget(personFocus),
                entryIds.size(),
                entityLinked.size()
        );

        final Map<Integer, RagSearchResult> merged = new LinkedHashMap<>();
        for (final RagSearchResult result : entityLinked) {
            if (result == null || result.getJournalEntryId() == null) continue;
            merged.put(result.getJournalEntryId(), result);
        }
        for (final RagSearchResult result : results) {
            if (result == null || result.getJournalEntryId() == null) continue;
            merged.putIfAbsent(result.getJournalEntryId(), result);
        }
        return merged.values().stream().limit(topK).collect(Collectors.toList());
    }

    /**
     * 질문 문장을 보고 RAG 응답 의도를 가볍게 분류합니다.
     */
    private RagIntent detectRagIntent(final String queryText) {
        final String text = StringUtils.defaultString(queryText);
        if (StringUtils.containsAny(text,
                "의미", "통섭", "엮", "상징", "패턴", "흐름", "반복", "변화", "감정선",
                "어떤 존재", "어떤 역할", "어떻게 이어", "전체 맥락", "관통", "해석")) {
            return RagIntent.SYNTHESIS;
        }
        if (StringUtils.containsAny(text,
                "요약", "정리", "모아", "묶어", "최근", "전체적으로", "한번에", "돌아봐")) {
            return RagIntent.SUMMARY;
        }
        return RagIntent.LOOKUP;
    }

    /**
     * RAG 의도별 검색 폭을 반환합니다.
     */
    private int resolveRagTopK(final RagIntent intent) {
        if (intent == RagIntent.SYNTHESIS) return RAG_SYNTHESIS_TOP_K;
        if (intent == RagIntent.SUMMARY) return RAG_SUMMARY_TOP_K;
        return RAG_TOP_K;
    }

    /**
     * RAG 의도별 벡터 검색 최소 점수를 반환합니다.
     */
    private double resolveRagMinScore(final RagIntent intent) {
        if (intent == RagIntent.SYNTHESIS) return RAG_SYNTHESIS_MIN_SCORE;
        return RAG_MIN_SCORE;
    }

    /**
     * 의도에 맞는 RAG 컨텍스트 텍스트를 구성합니다.
     */
    private String buildRagContextText(
            final RagIntent intent,
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        if (intent == RagIntent.SYNTHESIS || intent == RagIntent.SUMMARY) {
            return buildSynthesisRagContextText(intent, results, personFocus);
        }
        return buildLookupRagContextText(results);
    }

    /**
     * 단순 조회형 RAG 컨텍스트를 구성합니다.
     */
    private String buildLookupRagContextText(final List<RagSearchResult> results) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            sb.append("[").append(i + 1).append("]\n");
            sb.append(StringUtils.abbreviate(StringUtils.defaultString(results.get(i).getEntity().getEmbeddingText()), RAG_TEXT_MAX_LENGTH));
            sb.append("\n\n");
        }
        return sb.toString().trim();
    }

    /**
     * 요약/통섭형 RAG 컨텍스트를 시간순으로 압축 구성합니다.
     */
    private String buildSynthesisRagContextText(
            final RagIntent intent,
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        final StringBuilder sb = new StringBuilder();
        sb.append("RAG_INTENT: ").append(intent.name()).append('\n');
        sb.append("아래 기록 묶음은 통섭을 위한 재료입니다. 단일 기록만 보지 말고 반복, 변화, 연결을 함께 보세요.\n\n");
        appendPersonFocusBlock(sb, personFocus, results);
        appendTagSummaryBlock(sb, results, personFocus);
        appendTimelineSummaryBlock(sb, results);
        for (int i = 0; i < results.size(); i++) {
            final RagSearchResult result = results.get(i);
            if (result == null || result.getEntity() == null) continue;

            sb.append("- [").append(i + 1).append("] ");
            sb.append("date=").append(result.getEntity().getJournalDate()).append("; ");
            sb.append("kind=").append(result.getEntity().getContentKind()).append("; ");
            sb.append("match=").append(result.getMatchType()).append("; ");
            sb.append("score=").append(formatScore(result.getScore())).append("; ");
            if (result.getMatchedTokens() != null && !result.getMatchedTokens().isEmpty()) {
                sb.append("tokens=").append(result.getMatchedTokens()).append("; ");
            }
            sb.append("snippet=");
            sb.append(StringUtils.abbreviate(
                    StringUtils.defaultIfBlank(result.getSnippet(), result.getEntity().getEmbeddingText()),
                    resolveSynthesisSnippetMaxLength(result, personFocus)
            ));
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    /**
     * ?몃Ъ ?섎? 吏덈Ц?먯꽌 吏곸젒 ?몃?瑜?援щ텇?섎뒗 source瑜? ?곗꽑 ?댁꽍?섍쾶 蹂댁“ 釉붾줉??異붽??⑸땲??.
     */
    private void appendPersonFocusBlock(
            final StringBuilder sb,
            final PersonFocus personFocus,
            final List<RagSearchResult> results
    ) {
        if (personFocus == null) return;

        final List<RagSearchResult> focusedResults = resolvePersonFocusedResults(results, personFocus);
        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary = personFocus.entitySummary();

        final RagTimelineSummary timelineSummary = buildRagTimelineSummary(focusedResults);
        final Map<String, Integer> focusedTagCountMap = new LinkedHashMap<>();
        for (final RagSearchResult result : focusedResults) {
            incrementTagCounts(
                    focusedTagCountMap,
                    filterPersonMeaningTags(extractSourceTags(result), personFocus).stream()
                            .distinct()
                            .collect(Collectors.toList())
            );
        }

        sb.append("## PERSON_FOCUS\n");
        sb.append("target=").append(resolvePersonFocusTarget(personFocus)).append('\n');
        if (personFocus.tokens().size() > 1) {
            sb.append("aliases=").append(personFocus.tokens()).append('\n');
        }
        if (entitySummary != null) {
            sb.append("entity_mentions=").append(entitySummary.mentionCount()).append('\n');
            sb.append("entity_entries=").append(entitySummary.journalEntryCount()).append('\n');
            if (StringUtils.isNotBlank(entitySummary.firstDate()) || StringUtils.isNotBlank(entitySummary.lastDate())) {
                sb.append("entity_timeline=")
                        .append(StringUtils.defaultIfBlank(entitySummary.firstDate(), "?"))
                        .append(" ~ ")
                        .append(StringUtils.defaultIfBlank(entitySummary.lastDate(), "?"))
                        .append('\n');
            }
            if (entitySummary.contentKindCountMap() != null && !entitySummary.contentKindCountMap().isEmpty()) {
                sb.append("entity_content_kinds=").append(formatTopTags(entitySummary.contentKindCountMap(), 6)).append('\n');
            }
            final List<String> topRoles = entitySummary.topRoles(6);
            if (!topRoles.isEmpty()) {
                sb.append("top_roles=").append(topRoles).append('\n');
            }
            final List<String> roleAxesKo = formatPersonRoleAxes(entitySummary.roleCountMap());
            if (!roleAxesKo.isEmpty()) {
                sb.append("role_axes_ko=").append(roleAxesKo).append('\n');
            }
            final List<String> surfaceForms = entitySummary.topSurfaceForms(6);
            if (!surfaceForms.isEmpty()) {
                sb.append("surface_forms=").append(surfaceForms).append('\n');
            }
        }
        sb.append("matched_sources=").append(focusedResults.size()).append("/").append(results == null ? 0 : results.size()).append('\n');
        if (StringUtils.isNotBlank(timelineSummary.firstDate()) || StringUtils.isNotBlank(timelineSummary.lastDate())) {
            sb.append("timeline=")
                    .append(StringUtils.defaultIfBlank(timelineSummary.firstDate(), "?"))
                    .append(" ~ ")
                    .append(StringUtils.defaultIfBlank(timelineSummary.lastDate(), "?"))
                    .append('\n');
        }
        if (!timelineSummary.contentKindCountMap().isEmpty()) {
            sb.append("content_kinds=").append(formatTopTags(timelineSummary.contentKindCountMap(), 6)).append('\n');
        }
        if (!focusedTagCountMap.isEmpty()) {
            sb.append("repeated_tags=").append(formatTopTags(focusedTagCountMap, 8)).append('\n');
        }
        appendPersonMeaningScaffold(sb, personFocus, focusedResults, focusedTagCountMap, entitySummary, timelineSummary);
    }


    /**
     * person-meaning 질문용 답변 골격을 PERSON_FOCUS 블록에 추가합니다.
     *
     * <p>모델이 빈 주제 분류로 도망가지 않도록 태그·역할 축·기록 유형을 답 구조로 고정합니다.</p>
     */
    private void appendPersonMeaningScaffold(
            final StringBuilder sb,
            final PersonFocus personFocus,
            final List<RagSearchResult> focusedResults,
            final Map<String, Integer> focusedTagCountMap,
            final JournalEntityFocusService.PersonEntityFocusSummary entitySummary,
            final RagTimelineSummary timelineSummary
    ) {
        sb.append("## PERSON_MEANING_SCAFFOLD\n");
        sb.append("아래 5개 항목 순서로 답하세요. 태그명·역할 축·기록 유형은 이 블록 값을 인용해 해석하세요.\n");
        sb.append("금지: '업무 협업', '조직 관계', '사내 문화', '전략적 고민'처럼 태그·기록 인용 없는 빈 주제 분류.\n");
        sb.append("의미 = 등장 장면 나열이 아니라, 내 기록 안에서 반복되는 축·정서 기능·상징 연결입니다.\n\n");

        final Map<String, Integer> scaffoldContentKindCountMap = new LinkedHashMap<>();
        if (entitySummary != null && entitySummary.contentKindCountMap() != null) {
            scaffoldContentKindCountMap.putAll(entitySummary.contentKindCountMap());
        } else if (timelineSummary != null && !timelineSummary.contentKindCountMap().isEmpty()) {
            scaffoldContentKindCountMap.putAll(timelineSummary.contentKindCountMap());
        }
        final List<String> scaffoldRoleAxesKo = entitySummary == null
                ? List.of()
                : formatPersonRoleAxes(entitySummary.roleCountMap());
        final String interpretiveLead = buildPersonMeaningInterpretiveLead(
                resolvePersonFocusTarget(personFocus),
                focusedTagCountMap != null ? focusedTagCountMap : Map.of(),
                scaffoldRoleAxesKo,
                scaffoldContentKindCountMap
        );
        if (StringUtils.isNotBlank(interpretiveLead)) {
            sb.append("해석 시드: ").append(interpretiveLead).append('\n');
        }

        sb.append("반복 축: ");
        if (focusedTagCountMap != null && !focusedTagCountMap.isEmpty()) {
            sb.append(formatTopTags(focusedTagCountMap, 6));
        } else {
            sb.append("(태그 없음 - 스니펫의 반복 주제로만 서술)");
        }
        sb.append('\n');

        sb.append("역할·기능: ");
        if (entitySummary != null) {
            final List<String> roleAxesKo = formatPersonRoleAxes(entitySummary.roleCountMap());
            if (!roleAxesKo.isEmpty()) {
                sb.append(String.join(", ", roleAxesKo));
            } else {
                sb.append("(역할 축 미추출 - 스니펫의 정서·관계 표현으로만 서술)");
            }
        } else {
            sb.append("(entity catalog 없음)");
        }
        sb.append('\n');

        sb.append("기록 유형: ");
        if (entitySummary != null && entitySummary.contentKindCountMap() != null && !entitySummary.contentKindCountMap().isEmpty()) {
            sb.append(formatTopTags(entitySummary.contentKindCountMap(), 6));
        } else if (timelineSummary != null && !timelineSummary.contentKindCountMap().isEmpty()) {
            sb.append(formatTopTags(timelineSummary.contentKindCountMap(), 6));
        } else {
            sb.append("(기록 유형 정보 없음)");
        }
        sb.append('\n');

        sb.append("근거 장면: ");
        if (focusedResults != null && !focusedResults.isEmpty()) {
            final int hintLimit = Math.min(2, focusedResults.size());
            for (int i = 0; i < hintLimit; i++) {
                if (i > 0) sb.append(" | ");
                sb.append(sanitizePersonMeaningSnippet(focusedResults.get(i), personFocus));
            }
        } else {
            sb.append("(직접 언급 스니펫 없음)");
        }
        sb.append('\n');

        sb.append("확정 못 하는 것: 실제 직장 관계·직함·현실 신분은 기록에 직접 나올 때만 말하고, 없으면 '확인 안 됨'으로 명시\n\n");
    }

    /**
     * entity catalog 역할 축을 한국어 해석 라벨 목록으로 변환합니다.
     */
    private List<String> formatPersonRoleAxes(final Map<JournalEntityRoleType, Integer> roleCountMap) {
        if (roleCountMap == null || roleCountMap.isEmpty()) return List.of();

        return roleCountMap.entrySet().stream()
                .sorted(Map.Entry.<JournalEntityRoleType, Integer>comparingByValue().reversed())
                .limit(6)
                .map(entry -> formatPersonRoleAxis(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 단일 역할 축을 빈도와 함께 한국어 라벨로 포맷합니다.
     */
    private String formatPersonRoleAxis(final JournalEntityRoleType roleType, final int count) {
        final String label = PERSON_ROLE_AXIS_LABELS.getOrDefault(roleType, roleType.name());
        return label + "(" + count + ")";
    }

    /**
     * person-meaning source는 더 긴 스니펫을 허용합니다.
     */
    private int resolveSynthesisSnippetMaxLength(final RagSearchResult result, final PersonFocus personFocus) {
        if (personFocus != null && mentionsPersonFocus(result, personFocus)) {
            return RAG_PERSON_FOCUS_SNIPPET_MAX_LENGTH;
        }
        return RAG_SYNTHESIS_TEXT_MAX_LENGTH;
    }
    /**
     * 통섭 컨텍스트 상단에 태그 요약 블록을 추가합니다.
     *
     * <p>person-meaning 질문에서는 focused source와 person 태그만 집계해 LLM 노이즈를 줄입니다.</p>
     */
    private void appendTagSummaryBlock(
            final StringBuilder sb,
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        final RagTagSummary tagSummary = buildRagTagSummary(results, personFocus);

        if (tagSummary.totalTagCountMap().isEmpty()) return;

        sb.append("## 태그 요약\n");
        if (personFocus != null) {
            sb.append("아래 태그는 ")
                    .append(resolvePersonFocusTarget(personFocus))
                    .append(" 관련 기록에서만 집계한 person 축 태그입니다.\n");
        }
        sb.append("태그는 사용자가 의도적으로 붙인 주제 축입니다. 본문보다 강한 해석 신호로 우선 참고하세요.\n");
        appendTagCountLine(sb, "전체 반복 태그", tagSummary.totalTagCountMap());
        appendTagCountLine(sb, "꿈 기록 태그", tagSummary.dreamTagCountMap());
        appendTagCountLine(sb, "일기 기록 태그", tagSummary.diaryTagCountMap());
        appendTagCountLine(sb, "노트 기록 태그", tagSummary.noteTagCountMap());
        appendTagPairLine(sb, "연결 태그", tagSummary.tagPairCountMap());
        sb.append('\n');
    }

    /**
     * RAG source들의 태그 빈도와 공동출현 쌍을 집계합니다.
     */
    private RagTagSummary buildRagTagSummary(final List<RagSearchResult> results) {
        return buildRagTagSummary(results, null);
    }

    /**
     * person-meaning 질문에서는 focused source와 person 태그만 집계합니다.
     */
    private RagTagSummary buildRagTagSummary(final List<RagSearchResult> results, final PersonFocus personFocus) {
        final Map<String, Integer> totalTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> dreamTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> diaryTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> noteTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> tagPairCountMap = new LinkedHashMap<>();

        if (results == null) {
            return new RagTagSummary(totalTagCountMap, dreamTagCountMap, diaryTagCountMap, noteTagCountMap, tagPairCountMap);
        }

        final List<RagSearchResult> scopedResults = personFocus == null
                ? results
                : resolvePersonFocusedResults(results, personFocus);
        if (scopedResults.isEmpty()) {
            return new RagTagSummary(totalTagCountMap, dreamTagCountMap, diaryTagCountMap, noteTagCountMap, tagPairCountMap);
        }

        for (final RagSearchResult result : scopedResults) {
            if (result == null || result.getEntity() == null) continue;

            final List<String> tags = personFocus == null
                    ? extractSourceTags(result).stream().distinct().collect(Collectors.toList())
                    : filterPersonMeaningTags(extractSourceTags(result), personFocus).stream()
                            .distinct()
                            .collect(Collectors.toList());
            if (tags.isEmpty()) continue;

            incrementTagCounts(totalTagCountMap, tags);
            incrementTagPairCounts(tagPairCountMap, tags);

            final String contentKind = StringUtils.defaultString(result.getEntity().getContentKind());
            if ("DREAM".equalsIgnoreCase(contentKind)) {
                incrementTagCounts(dreamTagCountMap, tags);
            } else if ("DIARY".equalsIgnoreCase(contentKind)) {
                incrementTagCounts(diaryTagCountMap, tags);
            } else if ("NOTE".equalsIgnoreCase(contentKind)) {
                incrementTagCounts(noteTagCountMap, tags);
            }
        }

        return new RagTagSummary(totalTagCountMap, dreamTagCountMap, diaryTagCountMap, noteTagCountMap, tagPairCountMap);
    }

    /**
     * ?듭꽠 ?몃Ъ 吏덈Ц?댁? ?ㅼ떤 source?먯꽌 諛섎났?섎뒗 ?먮쫫 異?瑜?鍮좎묬?섏빞 ?섎뒗 寃쎌슦 person focus瑜?援ъ꽦?⑸땲??.
     */
    private PersonFocus resolvePersonFocus(
            final String queryText,
            final RagIntent intent,
            final List<RagSearchResult> results
    ) {
        if (intent != RagIntent.SYNTHESIS || !isPersonMeaningQuery(queryText)) return null;

        final List<String> tokens = extractPersonFocusTokens(queryText);
        if (tokens.isEmpty()) return null;
        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                journalEntityFocusService == null ? null : journalEntityFocusService.resolvePersonFocusSummary(tokens).orElse(null);
        final List<String> focusTokens = mergePersonFocusTokens(tokens, entitySummary);

        int matchedSourceCount = 0;
        for (final RagSearchResult result : results) {
            if (mentionsPersonTokens(result, focusTokens)) {
                matchedSourceCount++;
            }
        }
        if (matchedSourceCount <= 0 && entitySummary == null) return null;

        return new PersonFocus(resolvePersonFocusPrimaryToken(tokens, entitySummary), focusTokens, matchedSourceCount, entitySummary);
    }

    /**
     * query媛 person-centric synthesis吏덈Ц?몄? ?뺤씤?⑸땲??.
     */
    private boolean isPersonMeaningQuery(final String queryText) {
        final String text = StringUtils.defaultString(queryText);
        if (StringUtils.isBlank(text)) return false;
        return StringUtils.containsAny(text, PERSON_FOCUS_HINTS);
    }

    /**
     * person focus 濡? 留먰쓣 留? token ??異붿텧?⑸땲??.
     */
    private List<String> extractPersonFocusTokens(final String queryText) {
        final String normalized = StringUtils.defaultString(queryText).replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", " ");
        if (StringUtils.isBlank(normalized)) return List.of();

        final Set<String> seen = new HashSet<>();
        final List<String> tokens = new ArrayList<>();
        for (final String rawToken : normalized.split("\\s+")) {
            final String token = stripTrailingJosa(StringUtils.trimToEmpty(rawToken));
            if (StringUtils.length(token) < PERSON_FOCUS_MIN_TOKEN_LENGTH) continue;
            if (PERSON_FOCUS_STOPWORDS.contains(token)) continue;
            if (!containsReadablePersonToken(token)) continue;
            if (seen.add(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Merge query person tokens with canonical entity labels already normalized by the entity catalog.
     */
    private List<String> mergePersonFocusTokens(
            final List<String> tokens,
            final JournalEntityFocusService.PersonEntityFocusSummary entitySummary
    ) {
        final Set<String> seen = new LinkedHashSet<>();
        final List<String> mergedTokens = new ArrayList<>();

        if (tokens != null) {
            for (final String token : tokens) {
                if (StringUtils.isBlank(token) || !seen.add(token)) continue;
                mergedTokens.add(token);
            }
        }

        if (entitySummary == null) return mergedTokens;

        final String canonicalLabel = StringUtils.trimToEmpty(entitySummary.canonicalLabel());
        if (StringUtils.isNotBlank(canonicalLabel) && seen.add(canonicalLabel)) {
            mergedTokens.add(canonicalLabel);
        }

        for (final String surfaceForm : entitySummary.topSurfaceForms(4)) {
            if (StringUtils.isBlank(surfaceForm) || !seen.add(surfaceForm)) continue;
            mergedTokens.add(surfaceForm);
        }

        return mergedTokens;
    }

    /**
     * Prefer the catalog canonical label as the primary focus target once the entity summary exists.
     */
    private String resolvePersonFocusPrimaryToken(
            final List<String> tokens,
            final JournalEntityFocusService.PersonEntityFocusSummary entitySummary
    ) {
        if (entitySummary != null && StringUtils.isNotBlank(entitySummary.canonicalLabel())) {
            return entitySummary.canonicalLabel();
        }
        return tokens == null || tokens.isEmpty() ? null : tokens.get(0);
    }

    /**
     * person focus 媛 ?곸꽦?섏씠 source瑜? ?곗꽑 ?몃Ъ 洹쇨굅濡? ?좎젹?⑸땲??.
     */
    private List<RagSearchResult> prioritizeResultsForPersonFocus(
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        if (personFocus == null || results == null || results.isEmpty()) return results;

        return results.stream()
                .sorted(Comparator
                        .comparing((RagSearchResult result) -> resolvePersonFocusMatchPriority(result, personFocus), Comparator.reverseOrder())
                        .thenComparing(result -> countDirectPersonTokenMatches(result, personFocus), Comparator.reverseOrder())
                        .thenComparing(result -> result.getScore() == null ? 0D : result.getScore(), Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    /**
     * person-meaning RAG 정렬 우선순위: TAG > ENTITY > person 태그 > 본문 언급.
     */
    private int resolvePersonFocusMatchPriority(final RagSearchResult result, final PersonFocus personFocus) {
        if (result == null || personFocus == null) return 0;
        if (RagSearchResult.MATCH_TYPE_TAG.equals(result.getMatchType())) return 40;
        if (RagSearchResult.MATCH_TYPE_ENTITY.equals(result.getMatchType())) return 30;
        if (hasPersonTagMatch(result, personFocus)) return 25;
        if (mentionsPersonFocus(result, personFocus)) return 20;
        return 0;
    }

    /**
     * source payload 태그에 person focus 토큰이 포함되어 있는지 확인합니다.
     */
    private boolean hasPersonTagMatch(final RagSearchResult result, final PersonFocus personFocus) {
        return !filterPersonMeaningTags(extractSourceTags(result), personFocus).isEmpty();
    }

    /**
     * source媛 person focus ?좏겙???ы븿?섎뒗吏 ?뺤씤?⑸땲??.
     */
    private boolean mentionsPersonFocus(final RagSearchResult result, final PersonFocus personFocus) {
        if (personFocus == null) return false;
        return mentionsPersonTokens(result, personFocus.tokens());
    }

    /**
     * Resolve the display target for one PERSON_FOCUS block.
     */
    private String resolvePersonFocusTarget(final PersonFocus personFocus) {
        if (personFocus == null) return null;
        if (personFocus.entitySummary() != null && StringUtils.isNotBlank(personFocus.entitySummary().canonicalLabel())) {
            return personFocus.entitySummary().canonicalLabel();
        }
        return personFocus.primaryToken();
    }

    /**
     * source ?띿뒪?몄? person token???ы븿?섎뒗吏 ?뺤씤?⑸땲??.
     */
    private boolean mentionsPersonTokens(final RagSearchResult result, final List<String> tokens) {
        if (result == null || result.getEntity() == null || tokens == null || tokens.isEmpty()) return false;

        final String sourceText = buildPersonFocusSourceText(result);
        for (final String token : tokens) {
            if (StringUtils.isBlank(token)) continue;
            if (StringUtils.contains(sourceText, token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * KEYWORD 留ㅼ묶 source?먯꽌 person token?댁? 紐낆떆?곸쑝濡?踰킮???섎뒗吏 移댁슫?⑸땲??.
     */
    private Integer countDirectPersonTokenMatches(final RagSearchResult result, final PersonFocus personFocus) {
        if (result == null || personFocus == null || result.getMatchedTokens() == null) return 0;

        int count = 0;
        for (final String matchedToken : result.getMatchedTokens()) {
            if (personFocus.tokens().contains(stripTrailingJosa(StringUtils.trimToEmpty(matchedToken)))) {
                count++;
            }
        }
        return count;
    }

    /**
     * person focus source 留ㅼ묶??蹂댁“ ?띿뒪?몃? ?섏쓽濡?紐⑥쑝?덈떎.
     */
    private String buildPersonFocusSourceText(final RagSearchResult result) {
        if (result == null || result.getEntity() == null) return "";

        final StringBuilder sb = new StringBuilder();
        appendSourcePart(sb, result.getEntity().getEmbeddingText());
        appendSourcePart(sb, result.getSnippet());
        appendSourcePart(sb, String.join(" ", extractSourceTags(result)));

        final Map<String, Object> payload = readEmbeddingPayload(result);
        appendSourcePart(sb, payload.get("chapterTitle"));
        appendSourcePart(sb, payload.get("chapterCategory"));
        appendSourcePart(sb, payload.get("entryTitle"));
        appendSourcePart(sb, payload.get("dreamProviderName"));
        return sb.toString();
    }

    /**
     * source ?띿뒪?몃? person focus 寃???⑹슜鐏??쇰줈 異붽??⑸땲??.
     */
    private void appendSourcePart(final StringBuilder sb, final Object value) {
        final String text = StringUtils.defaultString(value == null ? null : String.valueOf(value));
        if (StringUtils.isBlank(text)) return;
        if (sb.length() > 0) sb.append(' ');
        sb.append(text);
    }

    /**
     * ?몃Ъ ?좏겙??濡쒓렇/湲곕줉 ?띿뒪?몃줈?쒕룄 ?뺤씤 媛?ν븳 由ы꽣?쒕? ?뺤씤?⑸땲??.
     */
    private boolean containsReadablePersonToken(final String token) {
        return token.matches(".*[\\p{IsAlphabetic}\\p{IsDigit}].*");
    }

    /**
     * ?ㅼ쭊 議곗궗瑜?諛쒓껄???쒗꽍 ?몃?濡??깅???먯쓽 ?좏겙?쇰줈 媛볥?섏떎.
     */
    private String stripTrailingJosa(final String token) {
        if (StringUtils.length(token) < PERSON_FOCUS_MIN_TOKEN_LENGTH) return token;

        final String[] suffixes = {
                "\uC5D0\uAC8C\uC11C", "\uC5D0\uAC8C",
                "\uD55C\uD14C\uC11C", "\uD55C\uD14C",
                "\uC5D0\uC11C",
                "\uC73C\uB85C\uB294", "\uC73C\uB85C",
                "\uB85C\uB294", "\uB85C",
                "\uC740", "\uB294", "\uC774", "\uAC00", "\uC744", "\uB97C",
                "\uACFC", "\uC640", "\uB3C4", "\uB9CC", "\uC758"
        };
        for (final String suffix : suffixes) {
            if (token.length() > suffix.length() + 1 && StringUtils.endsWith(token, suffix)) {
                return token.substring(0, token.length() - suffix.length());
            }
        }
        return token;
    }

    /**
     * 태그 카운트를 누적합니다.
     */
    private void incrementTagCounts(final Map<String, Integer> tagCountMap, final List<String> tags) {
        for (final String tag : tags) {
            if (StringUtils.isBlank(tag)) continue;
            tagCountMap.merge(tag, 1, Integer::sum);
        }
    }

    /**
     * 태그 공동출현 쌍 카운트를 누적합니다.
     */
    private void incrementTagPairCounts(final Map<String, Integer> tagPairCountMap, final List<String> tags) {
        if (tags.size() < 2) return;
        final List<String> sortedTags = tags.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        for (int i = 0; i < sortedTags.size(); i++) {
            for (int j = i + 1; j < sortedTags.size(); j++) {
                tagPairCountMap.merge(sortedTags.get(i) + " ↔ " + sortedTags.get(j), 1, Integer::sum);
            }
        }
    }

    /**
     * 태그 카운트 한 줄을 추가합니다.
     */
    private void appendTagCountLine(final StringBuilder sb, final String label, final Map<String, Integer> tagCountMap) {
        if (tagCountMap.isEmpty()) return;
        sb.append(label).append(": ").append(formatTopTags(tagCountMap, 14)).append('\n');
    }

    /**
     * 태그 공동출현 쌍 한 줄을 추가합니다.
     */
    private void appendTagPairLine(final StringBuilder sb, final String label, final Map<String, Integer> tagPairCountMap) {
        if (tagPairCountMap.isEmpty()) return;
        sb.append(label).append(": ").append(formatTopTags(tagPairCountMap, 10)).append('\n');
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
     * 메시지 metadata에 저장할 태그 요약 정보를 구성합니다.
     */
    private Map<String, Object> buildTagSummaryMetadata(final List<RagSearchResult> results) {
        return buildTagSummaryMetadata(results, null);
    }

    private Map<String, Object> buildTagSummaryMetadata(
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        final RagTagSummary tagSummary = buildRagTagSummary(results, personFocus);
        final Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("totalTags", buildTopTagCountMetadata(tagSummary.totalTagCountMap(), 20));
        metadata.put("dreamTags", buildTopTagCountMetadata(tagSummary.dreamTagCountMap(), 20));
        metadata.put("diaryTags", buildTopTagCountMetadata(tagSummary.diaryTagCountMap(), 20));
        metadata.put("noteTags", buildTopTagCountMetadata(tagSummary.noteTagCountMap(), 20));
        metadata.put("tagPairs", buildTopTagCountMetadata(tagSummary.tagPairCountMap(), 20));
        return metadata;
    }

    /**
     * 통섭 컨텍스트 상단에 시간/유형 요약 블록을 추가합니다.
     */
    private void appendTimelineSummaryBlock(final StringBuilder sb, final List<RagSearchResult> results) {
        final RagTimelineSummary timelineSummary = buildRagTimelineSummary(results);
        if (timelineSummary.sourceCount() <= 0) return;

        sb.append("## 시간/유형 흐름\n");
        if (StringUtils.isNotBlank(timelineSummary.firstDate()) || StringUtils.isNotBlank(timelineSummary.lastDate())) {
            sb.append("기간: ")
                    .append(StringUtils.defaultIfBlank(timelineSummary.firstDate(), "?"))
                    .append(" ~ ")
                    .append(StringUtils.defaultIfBlank(timelineSummary.lastDate(), "?"))
                    .append('\n');
        }
        appendTagCountLine(sb, "기록 유형", timelineSummary.contentKindCountMap());
        appendTagCountLine(sb, "월별 밀도", timelineSummary.monthCountMap());
        sb.append("시간축은 주제가 어느 시기에 응집되거나 옮겨가는지 보는 보조 해석 축입니다.\n\n");
    }

    /**
     * RAG source들의 시간/유형 흐름을 집계합니다.
     */
    private RagTimelineSummary buildRagTimelineSummary(final List<RagSearchResult> results) {
        final Map<String, Integer> contentKindCountMap = new LinkedHashMap<>();
        final Map<String, Integer> monthCountMap = new LinkedHashMap<>();
        final List<Date> dateList = new ArrayList<>();

        if (results != null) {
            for (final RagSearchResult result : results) {
                if (result == null || result.getEntity() == null) continue;

                final String contentKind = StringUtils.defaultIfBlank(result.getEntity().getContentKind(), "UNKNOWN");
                contentKindCountMap.merge(contentKind, 1, Integer::sum);

                final Date journalDate = result.getEntity().getJournalDate();
                if (journalDate == null) continue;

                dateList.add(journalDate);
                monthCountMap.merge(formatDate(journalDate, "yyyy-MM"), 1, Integer::sum);
            }
        }

        dateList.sort(Date::compareTo);
        final String firstDate = dateList.isEmpty() ? null : formatDate(dateList.get(0), "yyyy-MM-dd");
        final String lastDate = dateList.isEmpty() ? null : formatDate(dateList.get(dateList.size() - 1), "yyyy-MM-dd");
        return new RagTimelineSummary(results == null ? 0 : results.size(), firstDate, lastDate, contentKindCountMap, monthCountMap);
    }

    /**
     * 메시지 metadata에 저장할 시간/유형 요약 정보를 구성합니다.
     */
    private Map<String, Object> buildTimelineSummaryMetadata(final List<RagSearchResult> results) {
        final RagTimelineSummary timelineSummary = buildRagTimelineSummary(results);
        final Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sourceCount", timelineSummary.sourceCount());
        metadata.put("firstDate", timelineSummary.firstDate());
        metadata.put("lastDate", timelineSummary.lastDate());
        metadata.put("contentKinds", buildTopTagCountMetadata(timelineSummary.contentKindCountMap(), 10));
        metadata.put("months", buildTopTagCountMetadata(timelineSummary.monthCountMap(), 24));
        return metadata;
    }

    /**
     * 날짜를 지정한 패턴 문자열로 변환합니다.
     */
    private String formatDate(final Date date, final String pattern) {
        if (date == null) return null;
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 태그 카운트 맵을 metadata 배열 형태로 변환합니다.
     */
    private List<Map<String, Object>> buildTopTagCountMetadata(final Map<String, Integer> tagCountMap, final int limit) {
        if (tagCountMap == null || tagCountMap.isEmpty()) return List.of();
        return tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> {
                    final Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * RAG 의도별 추가 프롬프트를 구성합니다.
     */
    private String buildIntentPrompt(final RagIntent intent) {
        if (intent == RagIntent.SYNTHESIS) {
            return String.join("\n",
                    "PERSON_FOCUS 또는 PERSON_MEANING_SCAFFOLD 블록이 있으면 그 값을 답의 골격으로 우선 사용하세요.",
                    "이 질문은 통섭형 질문입니다.",
                    "인물의 의미를 묻는 경우 PERSON_MEANING_SCAFFOLD의 5개 항목 순서로 답하세요.",
                    "금지: '업무 협업', '조직 관계', '사내 문화', '전략적 고민'처럼 태그·기록 인용 없는 빈 주제 분류.",
                    "의미는 등장 장면 나열이 아니라 스캐폴드의 반복 축·역할·기능·기록 유형 차이를 연결한 해석입니다.",
                    "내부 필드명(role_axes_ko, repeated_tags 등)이나 섹션 키를 답변 문장에 그대로 쓰지 마세요.",
                    "답변은 기록에 확인되는 범위 안에서 다음 관점으로 엮으세요: 반복 축, 역할/기능, 기록 유형 차이, 근거 장면, 확정 불가.",
                    "특히 인물의 의미를 묻는 경우, 그 사람이 실제 직장 상사/동료/연인/가족인지 같은 현실 관계 지위는 기록에 직접 나온 표현이 있을 때만 말하세요.",
                    "기록에 직접 나온 장면 단어와 태그 축을 근거로 삼고, 근거 없는 일반론이나 외부 인물 이미지로 빈칸을 채우지 마세요.",
                    "단정하지 말고 '기록상으로는', '반복해서 보이는 건'처럼 근거의 한계를 드러내세요.",
                    "기록 조각을 단순 나열하지 말고, 사용자가 자기 흐름을 이해할 수 있게 하나의 해석으로 정리하세요."
            );
        }
        if (intent == RagIntent.SUMMARY) {
            return "이 질문은 요약형 질문입니다. 기록 묶음에서 핵심 사건, 반복 주제, 눈에 띄는 변화를 간결하게 정리하세요.";
        }
        return "답변은 기록에 적힌 범위 안에서만 정리하고, 부족한 부분은 짧게 확인 질문을 덧붙이세요.";
    }

    /**
     * RAG가 어떤 기록을 근거로 선택했는지 운영 로그에 남깁니다.
     */
    private void logRagSources(final List<RagSearchResult> results) {
        for (int i = 0; i < results.size(); i++) {
            final RagSearchResult result = results.get(i);
            if (result == null || result.getEntity() == null) continue;

            log.info(
                    "AI RAG source rank={} entryId={} date={} matchType={} score={} tokens={} snippet={}",
                    i + 1,
                    result.getJournalEntryId(),
                    result.getEntity().getJournalDate(),
                    result.getMatchType(),
                    formatScore(result.getScore()),
                    result.getMatchedTokens(),
                    StringUtils.abbreviate(StringUtils.defaultString(result.getSnippet()), 160)
            );
        }
    }

    /**
     * AI 메시지에 저장할 RAG 출처 메타데이터 JSON을 구성합니다.
     */
    private String buildRagMetadataJson(final RagContext ragContext, final String responseMode) {
        if (ragContext == null) return null;
        try {
            final Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("responseMode", responseMode);
            metadata.put("ragIntent", ragContext.intent() == null ? null : ragContext.intent().name());
            metadata.put("ragSourceCount", ragContext.results() == null ? 0 : ragContext.results().size());
            metadata.put("personFocus", buildPersonFocusMetadata(ragContext.personFocus()));
            metadata.put("ragTagSummary", buildTagSummaryMetadata(ragContext.results(), ragContext.personFocus()));
            metadata.put("ragTimelineSummary", buildTimelineSummaryMetadata(ragContext.results()));
            metadata.put("ragSources", buildRagSourceMetadataList(ragContext.results()));
            return objectMapper.writeValueAsString(metadata);
        } catch (final Exception e) {
            log.warn("Failed to build RAG metadata JSON. error={}", e.getMessage());
            return null;
        }
    }

    /**
     * RAG 검색 결과를 메시지 메타데이터에 넣기 좋은 축약 구조로 변환합니다.
     */
    private List<Map<String, Object>> buildRagSourceMetadataList(final List<RagSearchResult> results) {
        if (results == null || results.isEmpty()) return List.of();

        final List<Map<String, Object>> sourceList = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            final RagSearchResult result = results.get(i);
            if (result == null || result.getEntity() == null) continue;

            final Map<String, Object> source = new LinkedHashMap<>();
            source.put("rank", i + 1);
            source.put("journalEntryId", result.getJournalEntryId());
            source.put("journalDate", result.getEntity().getJournalDate());
            source.put("contentKind", result.getEntity().getContentKind());
            source.put("matchType", result.getMatchType());
            source.put("score", result.getScore());
            source.put("matchedTokens", result.getMatchedTokens());
            source.put("tags", extractSourceTags(result));
            source.put("snippet", StringUtils.abbreviate(StringUtils.defaultString(result.getSnippet()), 220));
            sourceList.add(source);
        }
        return sourceList;
    }

    /**
     * Build message metadata for PERSON_FOCUS summaries.
     */
    private Map<String, Object> buildPersonFocusMetadata(final PersonFocus personFocus) {
        if (personFocus == null) return null;

        final Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("target", resolvePersonFocusTarget(personFocus));
        metadata.put("tokens", personFocus.tokens());
        metadata.put("matchedSourceCount", personFocus.matchedSourceCount());

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary = personFocus.entitySummary();
        if (entitySummary != null) {
            metadata.put("journalEntityId", entitySummary.journalEntityId());
            metadata.put("canonicalLabel", entitySummary.canonicalLabel());
            metadata.put("mentionCount", entitySummary.mentionCount());
            metadata.put("journalEntryCount", entitySummary.journalEntryCount());
            metadata.put("firstDate", entitySummary.firstDate());
            metadata.put("lastDate", entitySummary.lastDate());
            metadata.put("contentKinds", buildTopTagCountMetadata(entitySummary.contentKindCountMap(), 10));
            metadata.put("topRoles", entitySummary.topRoles(10));
            metadata.put("roleAxesKo", formatPersonRoleAxes(entitySummary.roleCountMap()));
            metadata.put("surfaceForms", entitySummary.topSurfaceForms(10));
            metadata.put("journalEntryIds", entitySummary.journalEntryIds());
        }
        return metadata;
    }

    /**
     * RAG source payload에서 태그 목록을 추출합니다.
     */
    private List<String> extractSourceTags(final RagSearchResult result) {
        if (result == null || result.getEntity() == null) return List.of();
        final Map<String, Object> payload = readEmbeddingPayload(result);
        final Object rawTags = payload.get("tags");
        if (rawTags == null) return List.of();

        final String tagText = StringUtils.normalizeSpace(String.valueOf(rawTags));
        if (StringUtils.isBlank(tagText)) return List.of();

        final List<String> tagList = new ArrayList<>();
        for (final String token : tagText.split("\\s+")) {
            if (StringUtils.isBlank(token)) continue;
            tagList.add(token);
        }
        return tagList;
    }

    /**
     * 임베딩 payload JSON을 Map으로 변환합니다.
     */
    private Map<String, Object> readEmbeddingPayload(final RagSearchResult result) {
        if (result == null || result.getEntity() == null || StringUtils.isBlank(result.getEntity().getEmbeddingPayloadJson())) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(result.getEntity().getEmbeddingPayloadJson(), new TypeReference<Map<String, Object>>() {});
        } catch (final Exception e) {
            log.debug("Failed to parse RAG source payload. journalEntryId={}", result.getJournalEntryId(), e);
            return Map.of();
        }
    }

    /**
     * 로그용 검색 점수를 소수점 넷째 자리까지 정리합니다.
     */
    private String formatScore(final Double score) {
        if (score == null) return "null";
        return String.format("%.4f", score);
    }

    /**
     * 이전 assistant 응답이 언어 규칙을 어긴 경우 다음 프롬프트 맥락에서 제외합니다.
     *
     * <p>이미 저장된 잘못된 응답이 다음 생성에 다시 들어가면 모델이 같은 언어 패턴을 따라갈 수 있습니다.</p>
     *
     * @param messages 최근 대화 메시지
     * @return 언어 규칙 위반 assistant 메시지를 대체한 맥락 메시지
     */
    private List<ChatMessageDto> sanitizeContextMessages(final List<ChatMessageDto> messages) {
        if (messages == null) return List.of();
        return messages.stream()
                .map(message -> {
                    if (!isAssistantRole(message.getRole()) || !containsDisallowedHanScript(message.getContent())) {
                        return message;
                    }
                    return message.toBuilder()
                            .content("[이전 AI 응답은 언어 규칙 위반으로 맥락에서 제외되었습니다.]")
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * assistant 역할 여부를 확인합니다.
     */
    private boolean isAssistantRole(final String role) {
        return StringUtils.equalsAnyIgnoreCase(role, "ASSISTANT", "AI", "SYSTEM");
    }

    /**
     * 한국어 응답에 섞이면 안 되는 한자/중국어 계열 문자가 포함되었는지 확인합니다.
     *
     * <p>한국어 일반 응답에서 한자 1자는 우연히 포함될 수 있으므로 2자 이상부터 차단합니다.</p>
     */
    private boolean containsDisallowedHanScript(final String text) {
        if (StringUtils.isBlank(text)) return false;
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeScript.of(text.charAt(i)) == Character.UnicodeScript.HAN) {
                count++;
                if (count >= 2) return true;
            }
        }
        return false;
    }


    /**
     * person-meaning 통섭 답변이 빈 분류·스캐폴드 유출·무관 태그 인용만 있는지 검사합니다.
     */
    private boolean isHollowPersonMeaningResponse(final String response, final RagContext ragContext) {
        if (ragContext == null || ragContext.intent() != RagIntent.SYNTHESIS || ragContext.personFocus() == null) {
            return false;
        }
        if (StringUtils.isBlank(response)) return true;
        if (isPersonMeaningScaffoldLeak(response)) return true;
        return !hasPersonMeaningEvidence(response, ragContext);
    }

    /**
     * person-meaning 답변에 내부 스캐폴드/메타 필드명이 새어 나왔는지 확인합니다.
     */
    private boolean isPersonMeaningScaffoldLeak(final String response) {
        if (StringUtils.isBlank(response)) return false;
        final String normalized = StringUtils.lowerCase(response);
        for (final String marker : PERSON_MEANING_SCAFFOLD_LEAK_MARKERS) {
            if (normalized.contains(StringUtils.lowerCase(marker))) return true;
        }
        return false;
    }

    /**
     * person-meaning 답변이 이번 질문의 스냅샷 태그·역할 축을 실제로 인용했는지 확인합니다.
     */
    private boolean hasPersonMeaningEvidence(final String response, final RagContext ragContext) {
        if (StringUtils.isBlank(response) || ragContext == null || ragContext.personFocus() == null) {
            return false;
        }

        final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext);
        for (final String tag : snapshot.repeatedTagCountMap().keySet()) {
            if (StringUtils.contains(response, tag)) return true;
        }
        for (final String roleAxis : snapshot.roleAxesKo()) {
            if (StringUtils.contains(response, roleAxis)) return true;
            final int parenIndex = roleAxis.indexOf('(');
            if (parenIndex > 0 && StringUtils.contains(response, roleAxis.substring(0, parenIndex))) {
                return true;
            }
        }

        if (!snapshot.repeatedTagCountMap().isEmpty() || !snapshot.roleAxesKo().isEmpty()) {
            return false;
        }
        return citesPersonMeaningSnippetEvidence(response, snapshot.evidenceSnippets());
    }

    /**
     * person 태그·역할 축이 없을 때 근거 스니펫 인용 여부를 확인합니다.
     */
    private boolean citesPersonMeaningSnippetEvidence(final String response, final List<String> evidenceSnippets) {
        if (StringUtils.isBlank(response) || evidenceSnippets == null || evidenceSnippets.isEmpty()) return false;

        for (final String snippet : evidenceSnippets) {
            final String probe = extractSnippetEvidenceProbe(snippet);
            if (StringUtils.isNotBlank(probe) && StringUtils.containsIgnoreCase(response, probe)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 스니펫에서 guard가 비교할 짧은 근거 구절을 추출합니다.
     */
    private String extractSnippetEvidenceProbe(final String snippet) {
        if (StringUtils.isBlank(snippet)) return "";

        final String normalized = StringUtils.normalizeSpace(StringUtils.defaultString(snippet).replace("...", " "));
        if (normalized.length() <= 12) return normalized;

        final int start = Math.max(0, (normalized.length() - 15) / 2);
        return normalized.substring(start, Math.min(normalized.length(), start + 15)).trim();
    }

    /**
     * person-meaning 해석에 쓸 인물 태그만 남깁니다.
     *
     * <p>인물 축 태그에는 canonical/surface 이름(예: 김원빈)이 포함된다는 Dreamdiary 태그 계약을 따릅니다.</p>
     */
    private List<String> filterPersonMeaningTags(final List<String> tags, final PersonFocus personFocus) {
        if (tags == null || tags.isEmpty()) return List.of();
        return tags.stream()
                .filter(tag -> isPersonRelevantTag(tag, personFocus))
                .collect(Collectors.toList());
    }

    /**
     * 태그 문자열에 person focus 토큰이 포함되어 있는지 확인합니다.
     */
    private boolean isPersonRelevantTag(final String tag, final PersonFocus personFocus) {
        if (StringUtils.isBlank(tag) || personFocus == null || personFocus.tokens() == null) return false;

        final String normalizedTag = StringUtils.lowerCase(StringUtils.deleteWhitespace(tag));
        if (StringUtils.contains(normalizedTag, "dreamdiary")) return false;

        for (final String token : personFocus.tokens()) {
            if (StringUtils.isBlank(token)) continue;
            final String normalizedToken = StringUtils.lowerCase(StringUtils.deleteWhitespace(token));
            if (StringUtils.isNotBlank(normalizedToken) && normalizedTag.contains(normalizedToken)) {
                return true;
            }
        }
        return false;
    }

    /**
     * person-meaning 집계에 쓸 source 목록을 반환합니다. person 태그가 있으면 태그 매칭만 우선합니다.
     */
    private List<RagSearchResult> resolvePersonFocusedResults(
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        if (personFocus == null || results == null || results.isEmpty()) return List.of();

        final List<RagSearchResult> tagFocusedResults = results.stream()
                .filter(result -> !filterPersonMeaningTags(extractSourceTags(result), personFocus).isEmpty())
                .collect(Collectors.toList());
        if (!tagFocusedResults.isEmpty()) return tagFocusedResults;

        return List.of();
    }

    /**
     * PERSON_MEANING_SCAFFOLD 재료를 답변용 스냅샷으로 집계합니다.
     */
    private PersonMeaningSnapshot buildPersonMeaningSnapshot(final RagContext ragContext) {
        final PersonFocus personFocus = ragContext.personFocus();
        final List<RagSearchResult> results = ragContext.results() == null ? List.of() : ragContext.results();
        final List<RagSearchResult> focusedResults = resolvePersonFocusedResults(results, personFocus);

        final Map<String, Integer> focusedTagCountMap = new LinkedHashMap<>();
        for (final RagSearchResult result : focusedResults) {
            incrementTagCounts(
                    focusedTagCountMap,
                    filterPersonMeaningTags(extractSourceTags(result), personFocus).stream()
                            .distinct()
                            .collect(Collectors.toList())
            );
        }

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                personFocus == null ? null : personFocus.entitySummary();
        final RagTimelineSummary timelineSummary = buildRagTimelineSummary(focusedResults);

        final Map<String, Integer> contentKindCountMap = new LinkedHashMap<>();
        if (entitySummary != null && entitySummary.contentKindCountMap() != null) {
            contentKindCountMap.putAll(entitySummary.contentKindCountMap());
        } else if (!timelineSummary.contentKindCountMap().isEmpty()) {
            contentKindCountMap.putAll(timelineSummary.contentKindCountMap());
        }

        final List<String> roleAxesKo = entitySummary == null
                ? List.of()
                : formatPersonRoleAxes(entitySummary.roleCountMap());

        final List<String> evidenceSnippets = new ArrayList<>();
        final int snippetLimit = Math.min(2, focusedResults.size());
        for (int i = 0; i < snippetLimit; i++) {
            evidenceSnippets.add(sanitizePersonMeaningSnippet(focusedResults.get(i), personFocus));
        }

        final String firstDate = entitySummary != null && StringUtils.isNotBlank(entitySummary.firstDate())
                ? entitySummary.firstDate()
                : timelineSummary.firstDate();
        final String lastDate = entitySummary != null && StringUtils.isNotBlank(entitySummary.lastDate())
                ? entitySummary.lastDate()
                : timelineSummary.lastDate();

        return new PersonMeaningSnapshot(
                focusedTagCountMap,
                roleAxesKo,
                contentKindCountMap,
                evidenceSnippets,
                firstDate,
                lastDate
        );
    }

    /**
     * person-meaning fallback용 근거 장면 텍스트를 정리합니다.
     *
     * <p>embedding_text 메타라인/HTML을 제거하고 본문 중심으로 축약합니다.</p>
     */
    private String sanitizePersonMeaningSnippet(final RagSearchResult result, final PersonFocus personFocus) {
        if (result == null || result.getEntity() == null) return "";

        String body = extractEmbeddingBodyText(result.getEntity().getEmbeddingText());
        if (StringUtils.isBlank(body)) {
            body = stripHtmlForPersonMeaning(StringUtils.defaultIfBlank(result.getSnippet(), ""));
        } else {
            body = stripHtmlForPersonMeaning(body);
        }
        body = StringUtils.normalizeSpace(body);
        if (StringUtils.isBlank(body)) return "";

        if (personFocus != null && personFocus.tokens() != null) {
            for (final String token : personFocus.tokens()) {
                if (StringUtils.isBlank(token)) continue;
                final int index = StringUtils.indexOfIgnoreCase(body, token);
                if (index < 0) continue;

                final int start = Math.max(0, index - 80);
                final int end = Math.min(body.length(), index + 200);
                final String prefix = start > 0 ? "..." : "";
                final String suffix = end < body.length() ? "..." : "";
                return StringUtils.abbreviate(prefix + body.substring(start, end) + suffix, 220);
            }
        }

        return StringUtils.abbreviate(body, 220);
    }

    /**
     * embedding_text에서 본문 라인만 추출합니다.
     */
    private String extractEmbeddingBodyText(final String embeddingText) {
        if (StringUtils.isBlank(embeddingText)) return "";

        final int markerIndex = embeddingText.indexOf("본문:");
        if (markerIndex < 0) return "";

        final String body = embeddingText.substring(markerIndex + "본문:".length()).trim();
        final int nextLabelIndex = body.indexOf("\n유형:");
        if (nextLabelIndex > 0) return body.substring(0, nextLabelIndex).trim();
        return body;
    }

    /**
     * person-meaning 스니펫에서 HTML/엔티티를 제거합니다.
     */
    private String stripHtmlForPersonMeaning(final String text) {
        if (StringUtils.isBlank(text)) return "";
        return StringUtils.normalizeSpace(
                StringUtils.defaultString(text)
                        .replace("&nbsp;", " ")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replaceAll("<[^>]+>", " ")
        );
    }

    /**
     * person-meaning 해석 리드 문단을 규칙 기반으로 조립합니다.
     */
    private String buildPersonMeaningInterpretiveLead(
            final String target,
            final Map<String, Integer> repeatedTagCountMap,
            final List<String> roleAxesKo,
            final Map<String, Integer> contentKindCountMap
    ) {
        if (StringUtils.isBlank(target)) return "";

        final List<String> leadParts = new ArrayList<>();
        if (repeatedTagCountMap != null && !repeatedTagCountMap.isEmpty()) {
            leadParts.add("주로 " + formatTopTags(repeatedTagCountMap, 3) + " 축에 묶여 있어");
        }
        if (roleAxesKo != null && !roleAxesKo.isEmpty()) {
            final String topRoleAxis = roleAxesKo.get(0);
            final int parenIndex = topRoleAxis.indexOf('(');
            final String roleLabel = parenIndex > 0 ? topRoleAxis.substring(0, parenIndex) : topRoleAxis;
            if (StringUtils.isNotBlank(roleLabel)) {
                leadParts.add(roleLabel + " 관점에서 반복돼");
            }
        }

        final String contentKindHint = formatPersonMeaningContentKindHint(contentKindCountMap);
        if (StringUtils.isNotBlank(contentKindHint)) {
            leadParts.add(contentKindHint);
        }
        if (leadParts.isEmpty()) return "";
        return "기록상 " + target + "은(는) " + String.join(", ", leadParts) + ".";
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

        if (dreamCount > diaryCount && dreamCount > noteCount) return "꿈 기록 쪽에서 더 자주 등장해";
        if (diaryCount > dreamCount && diaryCount > noteCount) return "일기 기록 쪽에서 더 자주 등장해";
        if (noteCount > dreamCount && noteCount > diaryCount) return "노트 기록 쪽에서 더 자주 등장해";
        if (dreamCount == diaryCount && dreamCount > 0) return "꿈과 일기 기록에서 비슷하게 등장해";
        return "";
    }

    /**
     * 모델이 빈 분류만 내놓았을 때 스캐폴드 데이터로 결정적 person-meaning 답변을 만듭니다.
     */
    private String buildPersonMeaningDeterministicFallback(final RagContext ragContext) {
        if (ragContext == null || ragContext.personFocus() == null) {
            return "지금 확인되는 기록만으로는 답하기 어려워요. 어떤 사람이나 맥락을 말하는 건지 조금만 더 알려줘.";
        }
        if (ragContext.results() == null || ragContext.results().isEmpty()) {
            final String target = resolvePersonFocusTarget(ragContext.personFocus());
            return "내 Dreamdiary에서 " + target + " 인물 태그가 붙은 기록을 찾지 못했어. "
                    + "본문에 이름만 나온 글은 person-meaning 해석 재료로 쓰지 않아. "
                    + "해당 인물 태그(예: #김원빈)를 붙인 기록을 먼저 확인해줘.";
        }

        final PersonFocus personFocus = ragContext.personFocus();
        final String target = resolvePersonFocusTarget(personFocus);
        final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext);
        final StringBuilder sb = new StringBuilder();

        final String interpretiveLead = buildPersonMeaningInterpretiveLead(
                target,
                snapshot.repeatedTagCountMap(),
                snapshot.roleAxesKo(),
                snapshot.contentKindCountMap()
        );
        if (StringUtils.isNotBlank(interpretiveLead)) {
            sb.append(interpretiveLead).append("\n\n");
        } else {
            sb.append("기록상으로 ").append(target).append("은(는) 내 Dreamdiary 안에서 이렇게 반복돼 보여.\n\n");
        }

        if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append("반복 축: ").append(formatTopTags(snapshot.repeatedTagCountMap(), 6)).append('\n');
            sb.append("해석: 위 태그는 내가 의도적으로 붙인 ")
                    .append(target)
                    .append(" 관련 축이야. 같은 장면의 다른 태그보다 이 축을 우선 보면 돼.\n");
        } else {
            sb.append("반복 축: ")
                    .append(target)
                    .append(" 이름이 들어간 person 태그가 아직 없어. 아래 근거 장면 중심으로만 보면 돼.\n");
        }

        if (!snapshot.roleAxesKo().isEmpty()) {
            sb.append("역할·기능: ").append(String.join(", ", snapshot.roleAxesKo())).append('\n');
        } else {
            sb.append("역할·기능: entity catalog에서 역할 축이 아직 추출되지 않았어.\n");
        }

        if (!snapshot.contentKindCountMap().isEmpty()) {
            sb.append("기록 유형: ").append(formatContentKindSpread(snapshot.contentKindCountMap())).append('\n');
        }

        if (StringUtils.isNotBlank(snapshot.firstDate()) || StringUtils.isNotBlank(snapshot.lastDate())) {
            sb.append("기간: ")
                    .append(StringUtils.defaultIfBlank(snapshot.firstDate(), "?"))
                    .append(" ~ ")
                    .append(StringUtils.defaultIfBlank(snapshot.lastDate(), "?"))
                    .append('\n');
        }

        if (!snapshot.evidenceSnippets().isEmpty()) {
            sb.append("근거 장면: ");
            for (int i = 0; i < snapshot.evidenceSnippets().size(); i++) {
                if (i > 0) sb.append(" / ");
                sb.append(snapshot.evidenceSnippets().get(i));
            }
            sb.append('\n');
        }

        sb.append("\n확정 못 하는 것: 실제 직장 관계, 직함, 조직 내 지위는 기록에 직접 적힌 표현이 없으면 알 수 없어.");
        return sb.toString().trim();
    }

    /**
     * DREAM/DIARY/NOTE 비율을 person-meaning fallback 문장으로 포맷합니다.
     */
    private String formatContentKindSpread(final Map<String, Integer> contentKindCountMap) {
        if (contentKindCountMap == null || contentKindCountMap.isEmpty()) return "";

        final List<String> parts = new ArrayList<>();
        appendContentKindPart(parts, contentKindCountMap, "DREAM", "\uAF08");
        appendContentKindPart(parts, contentKindCountMap, "DIARY", "\uC77C\uAE30");
        appendContentKindPart(parts, contentKindCountMap, "NOTE", "\uB178\uD2B8");
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
    /**
     * LLM이 재시도 후에도 언어 규칙을 어긴 경우 저장할 안전한 한국어 응답을 만듭니다.
     */
    private String buildLanguageFallback(final String userMessage, final RagContext ragContext) {
        log.warn("AI response language guard fallback. query={}", StringUtils.abbreviate(userMessage, 80));
        if (ragContext == null || StringUtils.isBlank(ragContext.text())) {
            return "지금 확인되는 기록만으로는 답하기 어려워요. 어떤 사람이나 맥락을 말하는 건지 조금만 더 알려줘.";
        }
        if (ragContext.intent() == RagIntent.SYNTHESIS) {
            return "관련 기록은 찾았지만, 응답 생성 중 언어가 섞여서 그대로 보여주지 않았어요. 통섭 답변으로 다시 정리할 수 있게 질문을 한 번만 더 보내줘.";
        }
        return "관련 기록은 일부 찾았지만, 응답 생성 중 언어가 섞여서 그대로 보여주지 않았어요. 질문을 조금 더 구체적으로 말해주면 한국어로 다시 정리해볼게.";
    }

    /**
     * AI 응답에서 마크다운 기호를 제거하고 일반 텍스트로 변환합니다.
     *
     * <p>채팅 버블이 플레인텍스트 렌더이므로 마크다운 기호가 그대로 노출되는 것을 방지합니다.
     * 처리 순서: 코드블록 → 인라인코드 → 제목 → 굵은글씨/기울임 → 목록 기호</p>
     *
     * @param text 원본 AI 응답 텍스트
     * @return 마크다운 기호가 제거된 일반 텍스트
     */
    private String stripMarkdown(final String text) {
        if (text == null) return null;
        return text
                // 코드블록 (```lang\n...\n```) → 내용만 추출
                .replaceAll("(?s)```[^\\n]*\\n?(.*?)```", "$1")
                // 인라인 코드 (`code`) → 내용만 추출
                .replaceAll("`([^`]+)`", "$1")
                // ATX 제목 (## 제목) → 제목 텍스트만
                .replaceAll("(?m)^#{1,6}\\s+", "")
                // 굵은글씨+기울임 (***text***) → 텍스트만
                .replaceAll("\\*{3}(.+?)\\*{3}", "$1")
                // 굵은글씨 (**text**, __text__) → 텍스트만
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("__(.+?)__", "$1")
                // 기울임 (*text*, _text_) → 텍스트만
                .replaceAll("(?<![\\*_])\\*([^\\*\\n]+)\\*(?![\\*])", "$1")
                .replaceAll("(?<![_])_([^_\\n]+)_(?![_])", "$1")
                // 순서 없는 목록 기호 (- / * / + 행 시작) → 기호 제거
                .replaceAll("(?m)^[\\-\\*\\+]\\s+", "")
                // 순서 있는 목록 기호 (1. 행 시작) → 기호 제거
                .replaceAll("(?m)^\\d+\\.\\s+", "")
                // 인용문 (> 행 시작) → 기호 제거
                .replaceAll("(?m)^>\\s?", "")
                // 수평선 (---, ***, ___) → 빈 줄
                .replaceAll("(?m)^([-\\*_]){3,}\\s*$", "")
                // 3개 이상 연속 빈 줄 → 2줄로 정리
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }


    /**
     * person-meaning fallback 생성에 쓰는 스캐폴드 스냅샷.
     */
    private record PersonMeaningSnapshot(
            Map<String, Integer> repeatedTagCountMap,
            List<String> roleAxesKo,
            Map<String, Integer> contentKindCountMap,
            List<String> evidenceSnippets,
            String firstDate,
            String lastDate
    ) {}
    /**
     * RAG 의도, 검색 결과, 프롬프트용 컨텍스트 텍스트를 함께 보관합니다.
     */
    private record RagContext(RagIntent intent, List<RagSearchResult> results, String text, PersonFocus personFocus) {
        private static RagContext empty(final RagIntent intent) {
            return new RagContext(intent, List.of(), null, null);
        }
    }

    /**
     * ?몃Ъ ?의? 吏덈Ц?먯꽌 ?곗꽑 source瑜? ?뺣젹?섎뒗 ?ㅼ썙??留ㅼ묶 ?뺣낫.
     */
    private record PersonFocus(
            String primaryToken,
            List<String> tokens,
            int matchedSourceCount,
            JournalEntityFocusService.PersonEntityFocusSummary entitySummary
    ) {}

    /**
     * RAG 태그 빈도/공동출현 요약.
     */
    private record RagTagSummary(
            Map<String, Integer> totalTagCountMap,
            Map<String, Integer> dreamTagCountMap,
            Map<String, Integer> diaryTagCountMap,
            Map<String, Integer> noteTagCountMap,
            Map<String, Integer> tagPairCountMap
    ) {}

    /**
     * RAG 시간/유형 흐름 요약.
     */
    private record RagTimelineSummary(
            int sourceCount,
            String firstDate,
            String lastDate,
            Map<String, Integer> contentKindCountMap,
            Map<String, Integer> monthCountMap
    ) {}
}
