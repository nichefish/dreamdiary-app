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
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import org.springframework.context.i18n.LocaleContextHolder;
import java.util.Locale;

import java.time.LocalDate;
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
    /** person-meaning/hybrid SNAPSHOT에 실을 근거 장면 최대 건수 */
    private static final int PERSON_MEANING_SNAPSHOT_EVIDENCE_LIMIT = 3;
    /** person-stance(태도) 질문 tag-only RAG에서 가져올 최대 저널 엔트리 수 */
    private static final int PERSON_STANCE_RAG_TOP_K = 50;
    /** person-stance(태도) 질문 SNAPSHOT 근거 장면 최대 건수 */
    private static final int PERSON_STANCE_SNAPSHOT_EVIDENCE_LIMIT = 20;
    /** person-stance SNAPSHOT 근거 장면 1건 최대 길이 */
    private static final int PERSON_STANCE_SNAPSHOT_SNIPPET_MAX_LENGTH = 400;
    /** person-stance SNAPSHOT 근거 장면 총 글자 상한 */
    private static final int PERSON_STANCE_SNAPSHOT_EVIDENCE_CHAR_BUDGET = 8000;
    /** entity catalog 역할 축을 한국어 해석 라벨로 변환 */
    private static final Map<JournalEntityRoleType, String> PERSON_ROLE_AXIS_LABELS = Map.ofEntries(
            Map.entry(JournalEntityRoleType.COLLABORATION, "협업·동행 축"),
            Map.entry(JournalEntityRoleType.TENSION, "긴장·경계 축"),
            Map.entry(JournalEntityRoleType.EVALUATION, "평가·인정 축"),
            Map.entry(JournalEntityRoleType.CARE, "위로·보호 축"),
            Map.entry(JournalEntityRoleType.CONFLICT, "갈등·대립 축"),
            Map.entry(JournalEntityRoleType.DESIRE, "원함·끌임 축"),
            Map.entry(JournalEntityRoleType.SYMBOLIC_FIGURE, "상징·대상 축"),
            Map.entry(JournalEntityRoleType.UNKNOWN, "미분류 축")
    );
    /** Maps chapter category codes to Korean labels for person-meaning fallback. */
    private static final Map<String, String> PERSON_CHAPTER_CATEGORY_LABELS = Map.of(
            "DYNAMICS", "역동",
            "INTERACTION", "상호작용"
    );
    /** person focus 토큰 최소 길이 */
    private static final int PERSON_FOCUS_MIN_TOKEN_LENGTH = 2;
    /** person-meaning 질문에서 person focus를 감지하는 문장 힌트 */
    private static final String[] PERSON_FOCUS_HINTS = {
            "내 기록", "기록에서", "내 대화",
            "어떤 의미", "무슨 의미",
            "어떤 존재", "어떤 역할",
            "왜 반복", "왜 자주",
            "어떻게 등장", "등장하는",
            "어떻게 생각", "생각하고",
            "어떤 감정", "어떤 마음", "어떤 느낌",
            "어떻게 느끼", "느끼고"
    };
    /** 인물 토큰 + '~에 대해 뭘 말해' 류 LOOKUP형 질문 힌트 */
    private static final String[] PERSON_ABOUT_LOOKUP_HINTS = {
            "에 대해", "뭘 말해", "무엇을 말해", "말해줘", "말해 줘",
            "뭐라고", "뭘 알려", "무엇을 알려",
            "알려줘", "알려 줘", "말해줄", "말해 줄"
    };
    /** person focus 토큰 추출 시 제외할 불용어 */
    private static final Set<String> PERSON_FOCUS_STOPWORDS = Set.of(
            "나는", "너는", "내", "나", "기록",
            "dreamdiary", "Dreamdiary", "AI",
            "의미", "등장", "등장해", "등장하는",
            "무슨", "어떤", "어떻게",
            "역할", "존재", "반복", "자주",
            "통섭", "해석", "요약", "정리",
            "말해줘", "말해", "보여줘",
            "대해", "관련", "전체", "맥락",
            "대화", "느낌", "등장하", "있니"
    );
    /** person-meaning 답변에 내부 스캐폴드/메타 필드명이 새어 나오면 degraded로 본다. */
    private static final String[] PERSON_MEANING_SCAFFOLD_LEAK_MARKERS = {
            "role_axes_ko", "roleaxesko", "repeated_tags", "PERSON_MEANING_SCAFFOLD",
            "1_반복축", "2_역할기능", "3_기록유형",
            "4_근거장면힌트", "5_확정불가", "entity catalog",
            "PERSON_STANCE_SCAFFOLD", "1_내태도", "2_반복패턴", "5_근거장면"
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

    /** LocaleContextHolder 기준 영어 UI 여부. Accept-Language en 과 axios 헤더에 대응한다. */
    private boolean isEnglishLocale() {
        final Locale locale = LocaleContextHolder.getLocale();
        return locale != null && locale.getLanguage().startsWith("en");
    }

    /** chat.ai.* 카탈로그 메시지를 현재 locale 로 조회한다. */
    private String chatMsg(final String key, final Object... args) {
        return MessageUtils.getMessage(key, args);
    }

    /** 세션별 프롬프트보다 우선 적용할 응답 안전 규칙 (locale 카탈로그). */
    private String responseGuardPrompt() {
        return chatMsg("chat.ai.guard.response-rules");
    }

    /** LLM 이 언어 규칙을 어긴 경우 1회 재시도할 때 추가하는 지시문 (locale 카탈로그). */
    private String languageRetryPrompt() {
        return chatMsg("chat.ai.guard.language-retry");
    }


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
                        MessageUtils.getMessage("common.result.success")
                )
        );

        // 3. AI 응답 생성 (RAG 컨텍스트 주입)
        final int recentMessageLimit = chatSettingService.getMyRecentMessageLimit();
        final RagContext ragContext = buildRagContext(message);

        if (isCancelled(sessionId)) {
            log.info("AI response cancelled. sessionId={}", sessionId);
            cancelFlags.remove(sessionId);
            return;
        }

        final String strippedResponse;
        final String responseMode;
        String guardDetail = null;
        String retryGuardDetail = null;
        if (shouldUseRulePrimaryPersonSynthesisResponse(ragContext, message)) {
            log.info("AI person synthesis hybrid. sessionId={}, stance={}, appearance={}",
                    sessionId, isPersonAttitudeQuery(message), isPersonAppearanceQuery(message));
            final ResolvedChatResponse resolved = resolvePersonSynthesisHybridResponse(
                    sessionId,
                    message,
                    ragContext,
                    recentMessageLimit
            );
            if (isCancelled(sessionId)) {
                log.info("AI response cancelled. sessionId={}", sessionId);
                cancelFlags.remove(sessionId);
                return;
            }
            strippedResponse = resolved.content();
            responseMode = resolved.responseMode();
            guardDetail = resolved.guardDetail();
            retryGuardDetail = resolved.retryGuardDetail();
        } else {
            final String systemPrompt = buildSystemPromptWithRag(
                    StringUtils.defaultIfBlank(session.getSystemPrompt(), chatSessionService.getDefaultSystemPrompt()),
                    ragContext,
                    message
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
                rawResponse = ollamaClient.chat(systemPrompt + languageRetryPrompt(), contextMessages);
            }

            if (isCancelled(sessionId)) {
                log.info("AI response cancelled. sessionId={}", sessionId);
                cancelFlags.remove(sessionId);
                return;
            }

            final ResolvedChatResponse resolved = resolveLlmChatResponse(
                    sessionId,
                    message,
                    ragContext,
                    systemPrompt,
                    contextMessages,
                    stripInternalRecordCitations(stripMarkdown(rawResponse))
            );
            strippedResponse = resolved.content();
            responseMode = resolved.responseMode();
            guardDetail = resolved.guardDetail();
            retryGuardDetail = resolved.retryGuardDetail();
        }
        final String aiResponse = strippedResponse;

        // 4. AI 메시지 저장
        final ChatMessageDto aiMessage = ChatMessageDto.builder()
                        .sessionId(sessionId)
                        .seq(chatMessageService.getNextSeq(sessionId))
                        .role("ASSISTANT")
                        .title("Dreamdiary AI")
                        .content(aiResponse)
                        .metadataJson(buildRagMetadataJson(ragContext, responseMode, guardDetail, retryGuardDetail))
                        .build();
        final ServiceResponse aiResult = chatMessageService.regist(aiMessage);
        chatSessionService.touchAfterMessage(sessionId, message);

        // 5. AI 메시지 broadcast
        messagingTemplate.convertAndSend(
                "/topic/chat/session/" + sessionId,
                AjaxResponse.fromResponseWithObj(
                        aiResult,
                        MessageUtils.getMessage("common.result.success")
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
        chatSessionService.getMySessionEntity(sessionId);
        cancelFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false)).set(true);
        log.info("AI response cancel requested after ownership validation. sessionId={}", sessionId);
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
                return buildPersonMeaningRagContext(queryText, intent);
            }
            return buildMergedRagContext(queryText, intent);
        } catch (final Exception e) {
            log.warn("RAG context search failed, proceeding without context. intent={}, error={}", intent, e.getMessage());
            return RagContext.empty(intent);
        }
    }

    /**
     * 키워드·벡터·person 태그·entity 연결을 병합한 RAG 컨텍스트를 구성합니다.
     */
    private RagContext buildMergedRagContext(final String queryText, final RagIntent intent) {
        // 태도 질문이 tag-only에서 merged 폴백으로 넘어와도 같은 확대 검색 폭(PERSON_STANCE_RAG_TOP_K)을
        // 쓰도록 queryText를 함께 넘긴다. (이전에는 intent만 넘겨 SYNTHESIS 기본 폭으로 좁아졌다.)
        final int topK = resolveRagTopK(intent, queryText);
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

        return new RagContext(intent, results, buildRagContextText(intent, results, personFocus, queryText), personFocus);
    }

    /**
     * person-meaning 질문은 인물 태그 매칭을 우선하고, 없으면 병합 검색으로 fallback 합니다.
     */
    private RagContext buildPersonMeaningRagContext(final String queryText, final RagIntent intent) {
        final RagContext tagFirstContext = buildPersonMeaningTagOnlyRagContext(queryText, intent);
        if (tagFirstContext.results() != null && !tagFirstContext.results().isEmpty()) {
            return tagFirstContext;
        }

        log.info("AI RAG person-meaning tag-only empty, falling back to merged synthesis retrieval. target={}",
                tagFirstContext.personFocus() == null ? null : tagFirstContext.personFocus().primaryToken());
        return buildMergedRagContext(queryText, intent);
    }

    /**
     * person-meaning SYNTHESIS questions use only journal entries whose payload tags match person focus tokens.
     *
     * <p>Entity catalog resolves alias tokens (for example {@code 민수} -> {@code 김민수}) but does not inject
     * catalog-linked entries, keyword hits, vector hits, or body-mention fallbacks.</p>
     */
    private RagContext buildPersonMeaningTagOnlyRagContext(final String queryText, final RagIntent intent) {
        final int topK = resolveRagTopK(intent, queryText);
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
                resolvePersonFocusPrimaryToken(queryText, queryTokens, entitySummary),
                focusTokens,
                tagResults.size(),
                entitySummary
        );

        if (tagResults.isEmpty()) {
            log.info("AI RAG person-meaning tag-only empty. target={}, tokens={}",
                    personFocus.primaryToken(), focusTokens);
            return new RagContext(
                    intent,
                    List.of(),
                    buildPersonMeaningTagOnlyEmptyContextText(personFocus, queryText),
                    personFocus
            );
        }

        log.info("AI RAG person-meaning tag-only built. target={}, tokens={}, tagCount={}",
                personFocus.primaryToken(), focusTokens, tagResults.size());
        logRagSources(tagResults);
        return new RagContext(
                intent,
                tagResults,
                buildRagContextText(intent, tagResults, personFocus, queryText),
                personFocus
        );
    }

    /**
     * person-meaning tag-only retrieval found zero tagged sources.
     */
    private String buildPersonMeaningTagOnlyEmptyContextText(final PersonFocus personFocus, final String queryText) {
        final StringBuilder sb = new StringBuilder();
        sb.append("RAG_INTENT: SYNTHESIS\n");
        sb.append("PERSON_MEANING_TAG_ONLY: true\n");
        sb.append(chatMsg("chat.ai.prompt.rag.tag-only-empty")).append("\n\n");
        // PERSON_FOCUS 블록 부착은 제거됨(convergence): 이 컨텍스트는 결과 0건이라 상위에서 merged
        // 검색으로 폴백하며 폐기되고, personFocus가 살아 있는 경로는 Path C로 가서 text를 소비하지 않는다.
        return sb.toString().trim();
    }

    /**
     * 기본 시스템 프롬프트에 RAG 컨텍스트를 추가합니다.
     *
     * @param basePrompt 세션 또는 기본 시스템 프롬프트
     * @param ragContext 저널 검색 결과 컨텍스트
     * @return RAG 컨텍스트가 포함된 최종 시스템 프롬프트
     */
    private String buildSystemPromptWithRag(
            final String basePrompt,
            final RagContext ragContext,
            final String queryText
    ) {
        final String guardedPrompt = StringUtils.defaultString(basePrompt) + responseGuardPrompt();
        if (ragContext == null || StringUtils.isBlank(ragContext.text())) return guardedPrompt;
        return guardedPrompt
                + chatMsg("chat.ai.prompt.rag.journal-block")
                + buildIntentPrompt(ragContext.intent(), queryText)
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
     * 인물 이름이 추출되고 '~에 대해 뭘 말해/알려줘' 류로 묻는 질문인지 확인합니다.
     */
    private boolean isPersonAboutLookupQuery(final String queryText) {
        if (extractPersonFocusTokens(queryText).isEmpty()) {
            return false;
        }
        return StringUtils.containsAny(StringUtils.defaultString(queryText), PERSON_ABOUT_LOOKUP_HINTS);
    }

    /**
     * 질문 문장을 보고 RAG 응답 의도를 가볍게 분류합니다.
     */
    private RagIntent detectRagIntent(final String queryText) {
        final String text = StringUtils.defaultString(queryText);
        if (isPersonAboutLookupQuery(text)) {
            return RagIntent.SYNTHESIS;
        }
        if (StringUtils.containsAny(text,
                "의미", "통섭", "엮", "상징", "패턴", "흐름", "반복", "변화", "감정선",
                "어떤 존재", "어떤 역할", "어떻게 이어", "전체 맥락", "관통", "해석",
                "어떻게 생각", "생각하고", "어떤 감정", "어떤 마음", "어떤 느낌",
                "어떻게 느끼", "느끼고")) {
            return RagIntent.SYNTHESIS;
        }
        if (StringUtils.containsAny(text,
                "요약", "정리", "모아", "묶어", "최근", "전체적으로", "한번에", "돌아봐")) {
            return RagIntent.SUMMARY;
        }
        return RagIntent.LOOKUP;
    }

    /**
     * RAG 의도·질문 유형별 검색 폭을 반환합니다.
     *
     * <p>태도 질문은 tag-only·merged 검색 모두에서 더 많은 인물 태그 기록을 가져옵니다.
     * (queryText 없이 intent만 받던 오버로드는 merged 경로가 queryText를 넘기게 되며 제거됨.)</p>
     */
    private int resolveRagTopK(final RagIntent intent, final String queryText) {
        if (intent == RagIntent.SYNTHESIS && isPersonAttitudeQuery(queryText)) {
            return PERSON_STANCE_RAG_TOP_K;
        }
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
            final PersonFocus personFocus,
            final String queryText
    ) {
        if (intent == RagIntent.SYNTHESIS || intent == RagIntent.SUMMARY) {
            return buildSynthesisRagContextText(intent, results, personFocus, queryText);
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
            final PersonFocus personFocus,
            final String queryText
    ) {
        final StringBuilder sb = new StringBuilder();
        sb.append("RAG_INTENT: ").append(intent.name()).append('\n');
        if (isPersonAttitudeQuery(queryText)) {
            sb.append(chatMsg("chat.ai.prompt.rag.synthesis.stance-intro")).append("\n\n");
        } else {
            sb.append(chatMsg("chat.ai.prompt.rag.synthesis.general-intro")).append("\n\n");
        }
        // PERSON_FOCUS·PERSON_MEANING_SCAFFOLD 블록은 제거됨(convergence): personFocus가 해결된
        // 질문은 항상 Path C(SNAPSHOT 프롬프트)로 가서 이 contextText를 소비하지 않고, 이 텍스트를
        // 소비하는 레거시 경로는 personFocus가 항상 null이라 블록이 렌더링될 수 없었다.
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

        sb.append(chatMsg("chat.ai.prompt.rag.tag-summary.header"));
        if (personFocus != null) {
            sb.append(chatMsg(
                    "chat.ai.prompt.rag.tag-summary.person-focus-intro",
                    resolvePersonFocusTarget(personFocus)
            ));
        }
        sb.append(chatMsg("chat.ai.prompt.rag.tag-summary.guide"));
        appendTagCountLine(sb, chatMsg("chat.ai.prompt.rag.tag-summary.label.total"), tagSummary.totalTagCountMap());
        appendTagCountLine(sb, chatMsg("chat.ai.prompt.rag.tag-summary.label.dream"), tagSummary.dreamTagCountMap());
        appendTagCountLine(sb, chatMsg("chat.ai.prompt.rag.tag-summary.label.diary"), tagSummary.diaryTagCountMap());
        appendTagCountLine(sb, chatMsg("chat.ai.prompt.rag.tag-summary.label.note"), tagSummary.noteTagCountMap());
        appendTagPairLine(sb, chatMsg("chat.ai.prompt.rag.tag-summary.label.pair"), tagSummary.tagPairCountMap());
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
     * 통섭 인물 질문에서 여러 source에 반복되는 인물 축을 좁혀야 할 때 person focus를 구성합니다.
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

        return new PersonFocus(resolvePersonFocusPrimaryToken(queryText, tokens, entitySummary), focusTokens, matchedSourceCount, entitySummary);
    }

    /**
     * query가 person-centric synthesis 질문인지 확인합니다.
     */
    private boolean isPersonMeaningQuery(final String queryText) {
        final String text = StringUtils.defaultString(queryText);
        if (StringUtils.isBlank(text)) return false;
        if (StringUtils.containsAny(text, PERSON_FOCUS_HINTS)) {
            return true;
        }
        return isPersonAboutLookupQuery(text);
    }

    /**
     * 1인칭 태도·자기인식 질문(나는 X를 어떻게 생각/느끼는지)인지 확인합니다.
     *
     * <p>person-meaning(상징·역할 축·등장 방식)과 구분해 Path C 태도 rich-trust 프롬프트·최소 게이트를 태웁니다.
     * (예전에는 PERSON_STANCE_SCAFFOLD·강경 가드를 태웠으나 Option A 수렴으로 제거됨.)
     * {@code 내 대화에서 X는 어떤 느낌으로 등장}처럼 범위+등장 질문은 false입니다.</p>
     */
    private boolean isPersonAttitudeQuery(final String queryText) {
        final String text = StringUtils.defaultString(queryText);
        if (StringUtils.isBlank(text) || extractPersonFocusTokens(queryText).isEmpty()) {
            return false;
        }
        if (isPersonAppearanceQuery(text)) {
            return false;
        }
        if (!hasExplicitFirstPersonSubjectMarker(text)) {
            return false;
        }
        return StringUtils.containsAny(text,
                "어떻게 생각", "생각하고",
                "어떤 감정", "어떤 마음", "어떤 느낌",
                "어떻게 느끼", "느끼고");
    }

    /**
     * 기록·대화 속 인물의 등장 방식·느낌·톤을 묻는 질문인지 확인합니다.
     *
     * <p>1인칭 태도 질문과 달리 주어가 인물({@code 지연님은 … 등장})이거나 {@code 내 대화/내 기록} 범위 질문입니다.</p>
     */
    private boolean isPersonAppearanceQuery(final String queryText) {
        final String text = StringUtils.defaultString(queryText);
        if (StringUtils.isBlank(text)) return false;
        if (StringUtils.containsAny(text,
                "등장", "등장하", "나타나", "나오는", "나오", "보이는", "보여")) {
            return true;
        }
        if (StringUtils.containsAny(text, "내 대화", "내 기록", "대화에서", "대화 속", "대화 안")) {
            return !hasExplicitFirstPersonSubjectMarker(text);
        }
        return false;
    }

    /**
     * {@code 나는/내가} 등 주어가 사용자 자신인 1인칭 표지가 있는지 확인합니다.
     *
     * <p>{@code 내 대화}·{@code 내 기록} 같은 소유 범위만으로는 true가 되지 않습니다.</p>
     */
    private boolean hasExplicitFirstPersonSubjectMarker(final String queryText) {
        return StringUtils.containsAny(StringUtils.defaultString(queryText),
                "나는", "내가", "나의", "나한테", "나에게");
    }

    /**
     * person focus로 삼을 후보 token을 추출합니다.
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
            final String queryText,
            final List<String> tokens,
            final JournalEntityFocusService.PersonEntityFocusSummary entitySummary
    ) {
        if (entitySummary != null && StringUtils.isNotBlank(entitySummary.canonicalLabel())) {
            return entitySummary.canonicalLabel();
        }
        return selectPrimaryPersonTokenFromQuery(queryText, tokens);
    }

    /**
     * 질문 문장에서 person focus 대상 이름 토큰을 고릅니다.
     *
     * <p>{@code 지연님}처럼 호칭이 붙은 토큰을 우선하고, {@code 대화} 같은 범위어는 건너뜁니다.</p>
     */
    private String selectPrimaryPersonTokenFromQuery(final String queryText, final List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return null;

        final String text = StringUtils.defaultString(queryText);
        for (final String token : tokens) {
            if (StringUtils.isBlank(token)) continue;
            if (text.contains(token + "님")) {
                return token;
            }
        }
        for (final String token : tokens) {
            if (StringUtils.isNotBlank(token)) {
                return token;
            }
        }
        return null;
    }

    /**
     * person focus 관련성이 높은 source를 우선 근거로 쓰도록 정렬합니다.
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
     * source가 person focus 토큰을 포함하는지 확인합니다.
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
     * source 텍스트가 person token을 포함하는지 확인합니다.
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
     * KEYWORD 매칭 source에서 person token이 직접 매칭되는 횟수를 계산합니다.
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
     * person focus source 매칭에 사용할 보조 텍스트를 수집합니다.
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
     * source 텍스트를 person focus 검색용 문자열에 추가합니다.
     */
    private void appendSourcePart(final StringBuilder sb, final Object value) {
        final String text = StringUtils.defaultString(value == null ? null : String.valueOf(value));
        if (StringUtils.isBlank(text)) return;
        if (sb.length() > 0) sb.append(' ');
        sb.append(text);
    }

    /**
     * person 토큰이 로그/기록 텍스트에서도 확인 가능한 리터럴인지 확인합니다.
     */
    private boolean containsReadablePersonToken(final String token) {
        return token.matches(".*[\\p{IsAlphabetic}\\p{IsDigit}].*");
    }

    /**
     * 흔한 조사를 제거해 해석 대상 이름의 토큰으로 정규화합니다.
     */
    private String stripTrailingJosa(final String token) {
        if (StringUtils.length(token) < PERSON_FOCUS_MIN_TOKEN_LENGTH) return token;

        final String[] suffixes = {
                "님께", "님에게", "님에", "님을", "님를", "님은", "님는", "님이", "님가", "님과", "님와", "님의",
                "에게서", "에게", "에서", "에는", "에게는", "께",
                "한테서", "한테",
                "으로는", "으로", "로는", "로",
                "님", "씨",
                "은", "는", "이", "가", "을", "를", "과", "와", "도", "만", "의"
        };

        String normalized = StringUtils.trimToEmpty(token);
        boolean changed = true;
        while (changed && normalized.length() >= PERSON_FOCUS_MIN_TOKEN_LENGTH) {
            changed = false;
            for (final String suffix : suffixes) {
                if (normalized.length() > suffix.length() + 1 && StringUtils.endsWith(normalized, suffix)) {
                    normalized = normalized.substring(0, normalized.length() - suffix.length());
                    changed = true;
                    break;
                }
            }
        }
        return normalized;
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
     * SYNTHESIS person 질문은 스냅샷 hybrid(LLM 해석 + rule-primary 폴백) 경로를 탑니다.
     */
    private boolean shouldUseRulePrimaryPersonSynthesisResponse(final RagContext ragContext, final String queryText) {
        return ragContext != null
                && ragContext.intent() == RagIntent.SYNTHESIS
                && ragContext.personFocus() != null
                && isPersonMeaningQuery(queryText);
    }

    /**
     * person SYNTHESIS 질문 유형에 맞는 규칙 기반 응답을 조립합니다.
     */
    private String buildRulePrimaryPersonSynthesisResponse(final RagContext ragContext, final String queryText) {
        if (isPersonAttitudeQuery(queryText)) {
            return buildPersonStanceDeterministicFallback(ragContext, queryText);
        }
        if (isPersonAppearanceQuery(queryText)) {
            return buildPersonAppearanceDeterministicFallback(ragContext);
        }
        return buildPersonMeaningDeterministicFallback(ragContext);
    }

    /**
     * person SYNTHESIS: 서버 스냅샷 집계 후 LLM 해석 1회를 시도하고, 가드 실패 시 RULE_PRIMARY로 폴백합니다.
     */
    private ResolvedChatResponse resolvePersonSynthesisHybridResponse(
            final Integer sessionId,
            final String message,
            final RagContext ragContext,
            final int recentMessageLimit
    ) throws Exception {
        final String rulePrimaryFallback = buildRulePrimaryPersonSynthesisResponse(ragContext, message);
        if (ragContext == null
                || ragContext.personFocus() == null
                || ragContext.results() == null
                || ragContext.results().isEmpty()) {
            return new ResolvedChatResponse(rulePrimaryFallback, "RULE_PRIMARY", "empty_tagged_sources");
        }

        final String systemPrompt = buildPersonSynthesisHybridSystemPrompt(ragContext, message);
        final List<ChatMessageDto> hybridContext = buildPersonSynthesisHybridContext(
                sessionId,
                message,
                recentMessageLimit
        );

        String rawResponse = ollamaClient.chat(systemPrompt, hybridContext);
        if (containsDisallowedHanScript(rawResponse)) {
            log.warn("AI person synthesis hybrid language guard retry. sessionId={}", sessionId);
            rawResponse = ollamaClient.chat(systemPrompt + languageRetryPrompt(), hybridContext);
        }

        String strippedResponse = stripInternalRecordCitations(stripMarkdown(rawResponse));
        if (containsDisallowedHanScript(strippedResponse)) {
            log.warn("AI person synthesis hybrid language fallback to rule-primary. sessionId={}", sessionId);
            return new ResolvedChatResponse(rulePrimaryFallback, "RULE_PRIMARY", "language_guard");
        }
        if (!isDegradedPersonResponse(strippedResponse, ragContext, message)) {
            return new ResolvedChatResponse(strippedResponse, "PERSON_SYNTHESIS_HYBRID", null);
        }

        final String firstGuardDetail = describePersonGuardFailure(strippedResponse, ragContext, message);
        log.warn("AI person synthesis hybrid degraded, retrying once. sessionId={}, guardDetail={}",
                sessionId, firstGuardDetail);
        final String retryResponse = stripInternalRecordCitations(stripMarkdown(ollamaClient.chat(
                systemPrompt + buildPersonMeaningRetryPrompt(ragContext, message, firstGuardDetail),
                hybridContext
        )));
        if (!containsDisallowedHanScript(retryResponse)
                && !isDegradedPersonResponse(retryResponse, ragContext, message)) {
            return new ResolvedChatResponse(retryResponse, "PERSON_SYNTHESIS_HYBRID", null);
        }

        final String retryGuardDetail = describePersonGuardFailure(retryResponse, ragContext, message);
        log.warn("AI person synthesis hybrid guard failed, rule-primary fallback. sessionId={}, guardDetail={}, retryGuardDetail={}",
                sessionId, firstGuardDetail, retryGuardDetail);
        return new ResolvedChatResponse(rulePrimaryFallback, "RULE_PRIMARY", firstGuardDetail, retryGuardDetail);
    }

    /**
     * person SYNTHESIS hybrid에 넣을 최근 대화 맥락을 구성합니다.
     *
     * <p>전체 세션 히스토리 대신 최근 몇 턴만 포함해 follow-up 질문을 보조합니다.</p>
     */
    private List<ChatMessageDto> buildPersonSynthesisHybridContext(
            final Integer sessionId,
            final String currentMessage,
            final int recentMessageLimit
    ) throws Exception {
        final int cappedLimit = Math.min(Math.max(recentMessageLimit, 2), 12);
        final List<ChatMessageDto> recent = sanitizeContextMessages(
                chatMessageService.getRecentContextMessages(sessionId, cappedLimit)
        );
        if (recent.isEmpty()) {
            return List.of(ChatMessageDto.builder().role("USER").content(currentMessage).build());
        }

        final int hybridTurnLimit = 5;
        final int start = Math.max(0, recent.size() - hybridTurnLimit);
        return new ArrayList<>(recent.subList(start, recent.size()));
    }

    /**
     * person SYNTHESIS hybrid 경로용 시스템 프롬프트를 만듭니다.
     *
     * <p>전체 RAG 덤프 대신 {@link PersonMeaningSnapshot}만 전달합니다. 태도 질문은 rich-trust 자유 산문을
     * 안내하고 최소 게이트({@link #isDegradedPersonStanceRichResponse(String)})만 적용합니다
     * (Option A 이전에는 4섹션 형식·2인칭 비춤을 가드가 강제했으나 수렴으로 완화됨).
     * 등장(appearance) 질문은 현행대로 4섹션 형식을 프롬프트로 안내합니다.</p>
     */
    private String buildPersonSynthesisHybridSystemPrompt(final RagContext ragContext, final String queryText) {
        final StringBuilder sb = new StringBuilder();
        sb.append(responseGuardPrompt());
        sb.append(chatMsg("chat.ai.prompt.rag.hybrid.header"));
        sb.append(chatMsg("chat.ai.prompt.rag.hybrid.rule.snapshot-only"));
        sb.append(chatMsg("chat.ai.prompt.rag.hybrid.rule.no-internal-fields"));
        sb.append(chatMsg("chat.ai.prompt.rag.hybrid.rule.tag-hash-only"));
        sb.append(chatMsg("chat.ai.prompt.rag.hybrid.rule.context-followup"));

        if (isPersonAttitudeQuery(queryText)) {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.stance.opening"));
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.stance.evidence"));
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.stance.format-free"));
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.stance.ground-only"));
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.stance.continue-interpretation"));
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.stance.length"));
        } else if (isPersonAppearanceQuery(queryText)) {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.appearance.question"));
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.appearance.format"));
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.appearance.forbidden"));
        } else {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.meaning.question"));
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.meaning.guide"));
        }

        sb.append(chatMsg("chat.ai.prompt.rag.hybrid.snapshot.header"));
        appendPersonSynthesisSnapshotBlock(sb, ragContext, queryText);
        return sb.toString().trim();
    }

    /**
     * hybrid LLM에 전달할 person-meaning 스냅샷 블록을 조립합니다.
     */
    private void appendPersonSynthesisSnapshotBlock(
            final StringBuilder sb,
            final RagContext ragContext,
            final String queryText
    ) {
        if (ragContext == null || ragContext.personFocus() == null) return;

        final PersonFocus personFocus = ragContext.personFocus();
        final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext, queryText);
        final String target = resolvePersonFocusTarget(personFocus);

        sb.append(chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.target"))
                .append(": ")
                .append(StringUtils.defaultString(target))
                .append('\n');

        if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.repeated-tags"))
                    .append(": ")
                    .append(formatTopTagsForDisplay(snapshot.repeatedTagCountMap(), 8))
                    .append('\n');
        }
        if (!snapshot.linkedContextTagCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.linked-context"))
                    .append(": ")
                    .append(formatTopTagsForDisplay(snapshot.linkedContextTagCountMap(), 8))
                    .append('\n');
        }
        if (!snapshot.chapterCategoryCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.chapter-category"))
                    .append(": ")
                    .append(formatChapterCategorySpread(snapshot.chapterCategoryCountMap()))
                    .append('\n');
        }
        if (!snapshot.roleAxesKo().isEmpty()) {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.role-axes"))
                    .append(": ")
                    .append(String.join(", ", snapshot.roleAxesKo()))
                    .append('\n');
        }
        if (!snapshot.contentKindCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.content-kind"))
                    .append(": ")
                    .append(formatContentKindSpread(snapshot.contentKindCountMap()))
                    .append('\n');
        }
        if (StringUtils.isNotBlank(snapshot.firstDate()) || StringUtils.isNotBlank(snapshot.lastDate())) {
            sb.append(chatMsg(
                    "chat.ai.prompt.rag.hybrid.snapshot.label.period",
                    StringUtils.defaultIfBlank(snapshot.firstDate(), "?"),
                    StringUtils.defaultIfBlank(snapshot.lastDate(), "?")
            ));
        }
        if (!snapshot.evidenceSnippets().isEmpty()) {
            final String evidenceLabel = isPersonAttitudeQuery(queryText)
                    ? chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.evidence")
                    : chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.evidence-short");
            sb.append(evidenceLabel).append(": ")
                    .append(String.join(" | ", snapshot.evidenceSnippets()))
                    .append('\n');
        }

        final String interpretiveSeed;
        if (isPersonAttitudeQuery(queryText)) {
            interpretiveSeed = buildPersonStanceInterpretiveLead(
                    target,
                    snapshot.repeatedTagCountMap(),
                    snapshot.roleAxesKo(),
                    snapshot.contentKindCountMap(),
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterCategoryCountMap()
            );
        } else if (isPersonAppearanceQuery(queryText)) {
            interpretiveSeed = buildPersonAppearanceInterpretiveLead(
                    target,
                    snapshot.repeatedTagCountMap(),
                    snapshot.roleAxesKo(),
                    snapshot.contentKindCountMap(),
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterCategoryCountMap()
            );
        } else {
            interpretiveSeed = buildPersonMeaningInterpretiveLead(
                    target,
                    snapshot.repeatedTagCountMap(),
                    snapshot.roleAxesKo(),
                    snapshot.contentKindCountMap(),
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterCategoryCountMap()
            );
        }
        if (StringUtils.isNotBlank(interpretiveSeed)) {
            sb.append(chatMsg("chat.ai.prompt.rag.hybrid.snapshot.label.interpretive-seed"))
                    .append(": ")
                    .append(interpretiveSeed)
                    .append('\n');
        }
    }
    /**
     * LLM 1차 응답에 언어·person hollow guard를 적용해 최종 본문과 responseMode를 만듭니다.
     */
    private ResolvedChatResponse resolveLlmChatResponse(
            final Integer sessionId,
            final String message,
            final RagContext ragContext,
            final String systemPrompt,
            final List<ChatMessageDto> contextMessages,
            final String initialStrippedResponse
    ) throws Exception {
        if (containsDisallowedHanScript(initialStrippedResponse)) {
            return new ResolvedChatResponse(
                    buildLanguageFallback(message, ragContext),
                    "LANGUAGE_FALLBACK",
                    "language_guard"
            );
        }
        if (!isDegradedPersonResponse(initialStrippedResponse, ragContext, message)) {
            return new ResolvedChatResponse(initialStrippedResponse, "LLM");
        }
        final String firstGuardDetail = describePersonGuardFailure(initialStrippedResponse, ragContext, message);
        log.warn("AI person response degraded, retrying once. sessionId={}, guardDetail={}",
                sessionId, firstGuardDetail);
        final String retryResponse = stripInternalRecordCitations(stripMarkdown(ollamaClient.chat(
                systemPrompt + buildPersonMeaningRetryPrompt(ragContext, message, firstGuardDetail),
                contextMessages
        )));
        if (!containsDisallowedHanScript(retryResponse)
                && !isDegradedPersonResponse(retryResponse, ragContext, message)) {
            return new ResolvedChatResponse(retryResponse, "LLM");
        }
        final String retryGuardDetail = describePersonGuardFailure(retryResponse, ragContext, message);
        log.warn("AI person retry still hollow, deterministic fallback applied. sessionId={}, guardDetail={}, retryGuardDetail={}",
                sessionId, firstGuardDetail, retryGuardDetail);
        // 태도(person-attitude) 질문은 detectRagIntent → SYNTHESIS 라우팅으로 Path C
        // (shouldUseRulePrimaryPersonSynthesisResponse)에서만 처리·폴백된다. 이 레거시 저하 경로는
        // LOOKUP 인물-의미/등장 질문만 도달하므로 PERSON_STANCE_FALLBACK 분기는 두지 않는다.
        if (isPersonAppearanceQuery(message)) {
            return new ResolvedChatResponse(
                    buildPersonAppearanceDeterministicFallback(ragContext),
                    "PERSON_APPEARANCE_FALLBACK",
                    firstGuardDetail,
                    retryGuardDetail
            );
        }
        return new ResolvedChatResponse(
                buildPersonMeaningDeterministicFallback(ragContext),
                "PERSON_MEANING_FALLBACK",
                firstGuardDetail,
                retryGuardDetail
        );
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
     * 사용자 응답용 태그 표시 문자열에서 [엔서클] 등 메타 접두를 제거합니다.
     */
    private String formatDisplayTag(final String rawTag) {
        if (StringUtils.isBlank(rawTag)) return "";
        String normalized = StringUtils.normalizeSpace(rawTag).replaceAll("\\[[^\\]]+\\]", "");
        final int hashIndex = normalized.indexOf('#');
        if (hashIndex >= 0) {
            normalized = normalized.substring(hashIndex);
        }
        return StringUtils.trim(normalized);
    }

    /**
     * 규칙 기반 person 응답에 쓸 태그 요약 문자열입니다.
     */
    private String formatTopTagsForDisplay(final Map<String, Integer> tagCountMap, final int limit) {
        return tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> formatDisplayTag(entry.getKey()) + "(" + entry.getValue() + ")")
                .filter(entry -> StringUtils.isNotBlank(entry) && !entry.startsWith("("))
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

        sb.append(chatMsg("chat.ai.prompt.rag.timeline.header"));
        if (StringUtils.isNotBlank(timelineSummary.firstDate()) || StringUtils.isNotBlank(timelineSummary.lastDate())) {
            sb.append(chatMsg(
                    "chat.ai.prompt.rag.timeline.period",
                    StringUtils.defaultIfBlank(timelineSummary.firstDate(), "?"),
                    StringUtils.defaultIfBlank(timelineSummary.lastDate(), "?")
            ));
        }
        appendTagCountLine(sb, chatMsg("chat.ai.prompt.rag.timeline.label.content-kind"), timelineSummary.contentKindCountMap());
        appendTagCountLine(sb, chatMsg("chat.ai.prompt.rag.timeline.label.month-density"), timelineSummary.monthCountMap());
        sb.append(chatMsg("chat.ai.prompt.rag.timeline.guide"));
    }

    /**
     * RAG source들의 시간/유형 흐름을 집계합니다.
     */
    private RagTimelineSummary buildRagTimelineSummary(final List<RagSearchResult> results) {
        final Map<String, Integer> contentKindCountMap = new LinkedHashMap<>();
        final Map<String, Integer> monthCountMap = new LinkedHashMap<>();
        final List<LocalDate> dateList = new ArrayList<>();

        if (results != null) {
            for (final RagSearchResult result : results) {
                if (result == null || result.getEntity() == null) continue;

                final String contentKind = StringUtils.defaultIfBlank(result.getEntity().getContentKind(), "UNKNOWN");
                contentKindCountMap.merge(contentKind, 1, Integer::sum);

                final LocalDate journalDate = result.getEntity().getJournalDate();
                if (journalDate == null) continue;

                dateList.add(journalDate);
                monthCountMap.merge(formatDate(journalDate, "yyyy-MM"), 1, Integer::sum);
            }
        }

        dateList.sort(LocalDate::compareTo);
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
    private String formatDate(final Object date, final String pattern) {
        if (date == null) return null;
        try {
            return new SimpleDateFormat(pattern).format(DateUtils.asDate(date));
        } catch (final Exception e) {
            return null;
        }
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
    private String buildIntentPrompt(final RagIntent intent, final String queryText) {
        // 태도 질문이 이 분기(레거시 일반 경로)에 오는 것은 personFocus 미해결(태그·기록·entity 단서가 없는
        // 인물) 케이스뿐이다. Path C hybrid 프롬프트와 같은 rich-trust 계약(자유 산문·2인칭 비춤·반환각만
        // 금지)을 적용하며, 예전 4개 항목 스캐폴드 골격·조언/톤 금지 레짐은 재도입하지 않는다.
        if (intent == RagIntent.SYNTHESIS && isPersonAttitudeQuery(queryText)) {
            return chatMsg("chat.ai.prompt.intent.stance");
        }
        // PERSON_FOCUS·PERSON_MEANING_SCAFFOLD 블록 참조는 제거됨(convergence): personFocus가 해결된
        // 질문은 Path C(SNAPSHOT 프롬프트)로 가므로, 이 분기(레거시 일반 경로)에서는 블록이 존재할 수 없다.
        if (intent == RagIntent.SYNTHESIS && isPersonAppearanceQuery(queryText) && isPersonMeaningQuery(queryText)) {
            return chatMsg("chat.ai.prompt.intent.appearance-meaning");
        }
        if (intent == RagIntent.SYNTHESIS) {
            return chatMsg("chat.ai.prompt.intent.synthesis");
        }
        if (intent == RagIntent.SUMMARY) {
            return chatMsg("chat.ai.prompt.intent.summary");
        }
        if (intent == RagIntent.LOOKUP && isPersonLookupQuery(queryText)) {
            return chatMsg("chat.ai.prompt.intent.lookup-person");
        }
        return chatMsg("chat.ai.prompt.intent.default");
    }

    private boolean isPersonLookupQuery(final String queryText) {
        return !extractPersonFocusTokens(queryText).isEmpty();
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
        return buildRagMetadataJson(ragContext, responseMode, null);
    }

    private String buildRagMetadataJson(final RagContext ragContext, final String responseMode, final String guardDetail) {
        return buildRagMetadataJson(ragContext, responseMode, guardDetail, null);
    }

    private String buildRagMetadataJson(
            final RagContext ragContext,
            final String responseMode,
            final String guardDetail,
            final String retryGuardDetail
    ) {
        if (ragContext == null) return null;
        try {
            final Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("responseMode", responseMode);
            if (StringUtils.isNotBlank(guardDetail)) {
                metadata.put("guardDetail", guardDetail);
            }
            if (StringUtils.isNotBlank(retryGuardDetail)) {
                metadata.put("retryGuardDetail", retryGuardDetail);
            }
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
        if (isEnglishLocale()) {
            return containsExcessiveHangulForEnglish(text) || containsDisallowedHanScriptCount(text, 2);
        }
        return containsDisallowedHanScriptCount(text, 2);
    }

    private boolean containsDisallowedHanScriptCount(final String text, final int threshold) {
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
    private boolean containsExcessiveHangulForEnglish(final String text) {
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
     * person-meaning 통섭 답변이 빈 분류·스캐폴드 유출·무관 태그 인용만 있는지 검사합니다.
     */
    private boolean isHollowPersonMeaningResponse(
            final String response,
            final RagContext ragContext,
            final String queryText
    ) {
        if (ragContext == null || ragContext.intent() != RagIntent.SYNTHESIS || ragContext.personFocus() == null) {
            return false;
        }
        if (StringUtils.isBlank(response)) return true;
        if (isPersonMeaningScaffoldLeak(response)) return true;
        if (isPersonAttitudeQuery(queryText)) {
            return isDegradedPersonStanceRichResponse(response);
        }
        if (isThirdPersonPersonalityProfile(response)) return true;
        if (isPersonMeaningQuoteParade(response)) return true;
        return !hasPersonMeaningEvidence(response, ragContext);
    }

    /**
     * 인용문·대사 나열만 있고 해석이 없는 person-meaning 답변을 감지합니다.
     */
    private boolean isPersonMeaningQuoteParade(final String response) {
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
    private String describePersonGuardFailure(
            final String response,
            final RagContext ragContext,
            final String queryText
    ) {
        if (StringUtils.isBlank(response)) return "empty_response";
        if (containsDisallowedHanScript(response)) return "language_guard";
        if (isPersonAttitudeQuery(queryText)) {
            return describePersonStanceRichGuardFailure(response);
        }
        if (isHollowPersonMeaningResponse(response, ragContext, queryText)) return "person_meaning_hollow";
        return "person_guard_rejected";
    }

    /**
     * 상대 성격을 3인칭으로 단정하는 HR식 프로필 답변을 감지합니다.
     */
    private boolean isThirdPersonPersonalityProfile(final String response) {
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
    private boolean isDegradedPersonResponse(
            final String response,
            final RagContext ragContext,
            final String queryText
    ) {
        if (isHollowPersonMeaningResponse(response, ragContext, queryText)) {
            return true;
        }
        if (ragContext == null || ragContext.intent() != RagIntent.LOOKUP) {
            return false;
        }
        if (!isPersonMeaningQuery(queryText) || extractPersonFocusTokens(queryText).isEmpty()) {
            return false;
        }
        return isGenericPersonBucketHallucination(response);
    }

    /**
     * 기록 근거 없이 조직·업무 같은 일반 분류만 나열한 LOOKUP 인물 답변을 감지합니다.
     */
    private boolean isGenericPersonBucketHallucination(final String response) {
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
    private boolean isDegradedPersonStanceRichResponse(final String response) {
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
    private String describePersonStanceRichGuardFailure(final String response) {
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
    private boolean isPersonMeaningScaffoldLeak(final String response) {
        if (StringUtils.isBlank(response)) return false;
        final String normalized = StringUtils.lowerCase(response);
        for (final String marker : PERSON_MEANING_SCAFFOLD_LEAK_MARKERS) {
            if (normalized.contains(StringUtils.lowerCase(marker))) return true;
        }
        return false;
    }

    /**
     * 1차 답변이 거부된 구체 사유를 재시도 프롬프트에 반영합니다.
     *
     * <p>{@link #describePersonGuardFailure(String, RagContext, String)} 코드와 동일한 토큰을 사용합니다.</p>
     */
    private void appendPersonGuardRetryHint(final StringBuilder sb, final String guardDetail) {
        if (StringUtils.isBlank(guardDetail)) {
            return;
        }
        sb.append(chatMsg("chat.ai.prompt.rag.retry.guard-detail-label", guardDetail));
        switch (guardDetail) {
            case "person_stance_too_short" -> sb.append(
                    chatMsg("chat.ai.prompt.rag.retry.hint.person-stance-too-short"));
            case "person_stance_scaffold_leak" -> sb.append(
                    chatMsg("chat.ai.prompt.rag.retry.hint.person-stance-scaffold-leak"));
            case "person_stance_generic_bucket" -> sb.append(
                    chatMsg("chat.ai.prompt.rag.retry.hint.person-stance-generic-bucket"));
            case "person_meaning_hollow" -> sb.append(
                    chatMsg("chat.ai.prompt.rag.retry.hint.person-meaning-hollow"));
            case "language_guard" -> sb.append(
                    chatMsg("chat.ai.prompt.rag.retry.hint.language-guard"));
            case "empty_response" -> sb.append(
                    chatMsg("chat.ai.prompt.rag.retry.hint.empty-response"));
            default -> sb.append(chatMsg("chat.ai.prompt.rag.retry.hint.default"));
        }
    }

    /**
     * Builds a one-shot retry prompt when person-meaning hollow guard rejects the first LLM answer.
     */
    private String buildPersonMeaningRetryPrompt(
            final RagContext ragContext,
            final String queryText,
            final String guardDetail
    ) {
        if (isPersonAttitudeQuery(queryText)) {
            return buildPersonStanceRetryPrompt(ragContext, queryText, guardDetail);
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(chatMsg("chat.ai.prompt.rag.retry.meaning.header"));
        appendPersonGuardRetryHint(sb, guardDetail);
        sb.append(chatMsg("chat.ai.prompt.rag.retry.meaning.rejected"));
        sb.append(chatMsg("chat.ai.prompt.rag.retry.meaning.cite-required"));
        sb.append(chatMsg("chat.ai.prompt.rag.retry.meaning.no-generic-bucket"));

        if (ragContext != null && ragContext.personFocus() != null) {
            final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext);
            if (!snapshot.repeatedTagCountMap().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.person-tags"))
                        .append(": ")
                        .append(formatTopTags(snapshot.repeatedTagCountMap(), 4))
                        .append('\n');
            }
            if (!snapshot.linkedContextTagCountMap().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.linked-context"))
                        .append(": ")
                        .append(formatTopTags(snapshot.linkedContextTagCountMap(), 4))
                        .append('\n');
            }
            if (!snapshot.chapterCategoryCountMap().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.chapter-category"))
                        .append(": ")
                        .append(formatChapterCategorySpread(snapshot.chapterCategoryCountMap()))
                        .append('\n');
            }
            if (!snapshot.contentKindCountMap().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.content-kind"))
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
    private String buildPersonStanceRetryPrompt(
            final RagContext ragContext,
            final String queryText,
            final String guardDetail
    ) {
        final StringBuilder sb = new StringBuilder();
        sb.append(chatMsg("chat.ai.prompt.rag.retry.stance.header"));
        appendPersonGuardRetryHint(sb, guardDetail);
        sb.append(chatMsg("chat.ai.prompt.rag.retry.stance.weak-evidence"));
        sb.append(chatMsg("chat.ai.prompt.rag.retry.stance.honest-summary"));
        sb.append(chatMsg("chat.ai.prompt.rag.retry.stance.opening-format"));
        sb.append(chatMsg("chat.ai.prompt.rag.retry.stance.no-fabrication"));

        if (ragContext != null && ragContext.personFocus() != null) {
            final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext, queryText);
            final String interpretiveLead = buildPersonStanceInterpretiveLead(
                    resolvePersonFocusTarget(ragContext.personFocus()),
                    snapshot.repeatedTagCountMap(),
                    snapshot.roleAxesKo(),
                    snapshot.contentKindCountMap(),
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterCategoryCountMap()
            );
            if (StringUtils.isNotBlank(interpretiveLead)) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.interpretive-seed"))
                        .append(": ")
                        .append(interpretiveLead)
                        .append('\n');
            }
            if (!snapshot.repeatedTagCountMap().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.person-tags"))
                        .append(": ")
                        .append(formatTopTags(snapshot.repeatedTagCountMap(), 6))
                        .append('\n');
            }
            if (!snapshot.linkedContextTagCountMap().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.linked-context"))
                        .append(": ")
                        .append(formatTopTags(snapshot.linkedContextTagCountMap(), 4))
                        .append('\n');
            }
            if (!snapshot.roleAxesKo().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.role-axes"))
                        .append(": ")
                        .append(String.join(", ", snapshot.roleAxesKo()))
                        .append('\n');
            }
            if (!snapshot.contentKindCountMap().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.content-kind"))
                        .append(": ")
                        .append(formatContentKindSpread(snapshot.contentKindCountMap()))
                        .append('\n');
            }
            if (!snapshot.evidenceSnippets().isEmpty()) {
                sb.append(chatMsg("chat.ai.prompt.rag.retry.label.evidence"))
                        .append(": ")
                        .append(String.join(" | ", snapshot.evidenceSnippets()))
                        .append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * person-meaning 답변이 이번 질문의 스냅샷 태그·역할 축·연결 맥락을 실제로 인용했는지 확인합니다.
     */
    private boolean hasPersonMeaningEvidence(final String response, final RagContext ragContext) {
        if (StringUtils.isBlank(response) || ragContext == null || ragContext.personFocus() == null) {
            return false;
        }

        final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext);
        if (citesPersonMeaningTagEvidence(response, snapshot.repeatedTagCountMap())) return true;
        if (citesPersonMeaningRoleAxisEvidence(response, snapshot.roleAxesKo())) return true;
        if (citesPersonMeaningTagEvidence(response, snapshot.linkedContextTagCountMap())) return true;
        if (citesPersonMeaningChapterCategoryEvidence(response, snapshot.chapterCategoryCountMap())) return true;
        if (citesPersonMeaningContentKindEvidence(response, snapshot.contentKindCountMap())) return true;
        return citesPersonMeaningSnippetEvidence(response, snapshot.evidenceSnippets());
    }

    /**
     * person-meaning 답변이 태그 전체 문자열 또는 # 후 핵심어를 인용했는지 확인합니다.
     */
    private boolean citesPersonMeaningTagEvidence(final String response, final Map<String, Integer> tagCountMap) {
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
    private boolean citesPersonMeaningRoleAxisEvidence(final String response, final List<String> roleAxesKo) {
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
     * person-meaning 답변이 책터 분류 코드/한국어 라벨을 인용했는지 확인합니다.
     */
    private boolean citesPersonMeaningChapterCategoryEvidence(
            final String response,
            final Map<String, Integer> chapterCategoryCountMap
    ) {
        if (StringUtils.isBlank(response) || chapterCategoryCountMap == null || chapterCategoryCountMap.isEmpty()) {
            return false;
        }

        for (final String chapterCategory : chapterCategoryCountMap.keySet()) {
            if (StringUtils.isBlank(chapterCategory)) continue;
            if (StringUtils.containsIgnoreCase(response, chapterCategory)) return true;

            final String label = PERSON_CHAPTER_CATEGORY_LABELS.get(chapterCategory);
            if (StringUtils.isNotBlank(label) && StringUtils.contains(response, label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * person-meaning 답변이 기록 유형(꾋/일기/노트) 표현을 인용했는지 확인합니다.
     */
    private boolean citesPersonMeaningContentKindEvidence(
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
    private String extractTagCitationProbe(final String tag) {
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
    private boolean citesPersonMeaningSnippetEvidence(final String response, final List<String> evidenceSnippets) {
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
    private List<String> extractSnippetEvidenceProbes(final String snippet) {
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

    /**
     * person-meaning 해석에 쓸 인물 태그만 남깁니다.
     *
     * <p>인물 축 태그에는 canonical/surface 이름(예: 김민수)이 포함된다는 Dreamdiary 태그 계약을 따릅니다.</p>
     */
    /**
     * person-meaning 해석에 쓸 인물 태그만 남깁니다.
     */
    private List<String> filterPersonMeaningTags(final List<String> tags, final PersonFocus personFocus) {
        return filterPersonMeaningTags(tags, personFocus, null);
    }

    /**
     * person-meaning 해석에 쓸 인물 태그만 남깁니다.
     *
     * <p>{@code dominantTagStem}이 있으면 짧은 이름 토큰(예: 지연)의 오매칭(#문지연)을 제거합니다.</p>
     */
    private List<String> filterPersonMeaningTags(
            final List<String> tags,
            final PersonFocus personFocus,
            final String dominantTagStem
    ) {
        if (tags == null || tags.isEmpty()) return List.of();
        return tags.stream()
                .filter(tag -> isPersonRelevantTag(tag, personFocus, dominantTagStem))
                .collect(Collectors.toList());
    }

    /**
     * 태그 문자열에 person focus 토큰이 포함되어 있는지 확인합니다.
     */
    private boolean isPersonRelevantTag(final String tag, final PersonFocus personFocus) {
        return isPersonRelevantTag(tag, personFocus, null);
    }

    /**
     * 태그 문자열이 person focus(및 선택적 dominant stem)와 맞는지 확인합니다.
     */
    private boolean isPersonRelevantTag(
            final String tag,
            final PersonFocus personFocus,
            final String dominantTagStem
    ) {
        if (StringUtils.isBlank(tag) || personFocus == null || personFocus.tokens() == null) return false;

        final String normalizedTag = StringUtils.lowerCase(StringUtils.deleteWhitespace(tag));
        if (StringUtils.contains(normalizedTag, "dreamdiary")) return false;

        if (StringUtils.isNotBlank(dominantTagStem)) {
            final String stem = extractPersonTagStem(tag);
            return StringUtils.isNotBlank(stem)
                    && stem.equalsIgnoreCase(StringUtils.deleteWhitespace(dominantTagStem));
        }

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
     * person 태그에서 # stem만 추출합니다.
     */
    private String extractPersonTagStem(final String tag) {
        final String displayTag = formatDisplayTag(tag);
        if (StringUtils.isBlank(displayTag)) return "";
        return displayTag.startsWith("#") ? displayTag.substring(1) : displayTag;
    }

    /**
     * 짧은 person 토큰(예: 지연)에 대해 반복 빈도가 가장 높은 태그 stem을 고릅니다.
     */
    private String resolveDominantPersonTagStem(
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        if (personFocus == null || results == null || results.isEmpty()) return null;

        final String primaryToken = StringUtils.trimToEmpty(personFocus.primaryToken());
        if (StringUtils.isBlank(primaryToken) || primaryToken.length() > 3) return null;

        final Map<String, Integer> stemCounts = new LinkedHashMap<>();
        for (final RagSearchResult result : results) {
            if (result == null) continue;
            for (final String tag : extractSourceTags(result)) {
                if (!isPersonRelevantTag(tag, personFocus, null)) continue;

                final String stem = extractPersonTagStem(tag);
                if (StringUtils.isBlank(stem)) continue;
                if (!stem.equalsIgnoreCase(primaryToken)
                        && !StringUtils.endsWithIgnoreCase(stem, primaryToken)) {
                    continue;
                }
                stemCounts.merge(stem, 1, Integer::sum);
            }
        }

        return stemCounts.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Keeps co-occurring scene tags from person-meaning sources while excluding person-focus tags.
     */
    private List<String> filterPersonMeaningLinkedContextTags(final List<String> tags, final PersonFocus personFocus) {
        if (tags == null || tags.isEmpty()) return List.of();
        return tags.stream()
                .filter(tag -> !isPersonRelevantTag(tag, personFocus))
                .collect(Collectors.toList());
    }

    /**
     * Aggregates linked context tags and chapter categories for person-meaning fallback/scaffold.
     */
    private PersonMeaningContextAggregates buildPersonMeaningContextAggregates(
            final List<RagSearchResult> focusedResults,
            final PersonFocus personFocus
    ) {
        final Map<String, Integer> linkedContextTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> chapterCategoryCountMap = new LinkedHashMap<>();
        if (focusedResults == null || focusedResults.isEmpty()) {
            return new PersonMeaningContextAggregates(linkedContextTagCountMap, chapterCategoryCountMap);
        }

        for (final RagSearchResult result : focusedResults) {
            if (result == null) continue;
            incrementTagCounts(
                    linkedContextTagCountMap,
                    filterPersonMeaningLinkedContextTags(extractSourceTags(result), personFocus)
            );
            final String chapterCategory = extractSourceChapterCategory(result);
            if (StringUtils.isNotBlank(chapterCategory)) {
                chapterCategoryCountMap.merge(chapterCategory, 1, Integer::sum);
            }
        }
        return new PersonMeaningContextAggregates(linkedContextTagCountMap, chapterCategoryCountMap);
    }

    /**
     * Reads chapter category code from a RAG source embedding payload.
     */
    private String extractSourceChapterCategory(final RagSearchResult result) {
        if (result == null || result.getEntity() == null) return "";
        final Map<String, Object> payload = readEmbeddingPayload(result);
        return StringUtils.trimToEmpty(String.valueOf(payload.getOrDefault("chapterCategory", "")));
    }

    /**
     * Appends role/function text from linked context tags and chapter categories.
     */
    private void appendPersonMeaningLinkedContextRoleHint(
            final StringBuilder sb,
            final Map<String, Integer> linkedContextTagCountMap,
            final Map<String, Integer> chapterCategoryCountMap,
            final boolean scaffoldStyle
    ) {
        if ((linkedContextTagCountMap == null || linkedContextTagCountMap.isEmpty())
                && (chapterCategoryCountMap == null || chapterCategoryCountMap.isEmpty())) {
            if (scaffoldStyle) {
                sb.append(chatMsg("chat.ai.fallback.linked-context.scaffold-empty"));
            } else {
                sb.append(chatMsg("chat.ai.fallback.linked-context.empty"));
            }
            return;
        }

        if (!scaffoldStyle) {
            sb.append(chatMsg("chat.ai.fallback.linked-context.prefix")).append(" ");
        }
        final List<String> hintParts = new ArrayList<>();
        if (linkedContextTagCountMap != null && !linkedContextTagCountMap.isEmpty()) {
            hintParts.add(formatTopTags(linkedContextTagCountMap, 3));
        }
        if (chapterCategoryCountMap != null && !chapterCategoryCountMap.isEmpty()) {
            hintParts.add(formatChapterCategorySpread(chapterCategoryCountMap));
        }
        sb.append(String.join(" · ", hintParts));
        if (scaffoldStyle) {
            sb.append(chatMsg("chat.ai.fallback.linked-context.suffix-scaffold"));
        } else {
            sb.append(chatMsg("chat.ai.fallback.linked-context.suffix"));
        }
    }

    /**
     * Formats chapter category counts for person-meaning sentences.
     */
    private String formatChapterCategorySpread(final Map<String, Integer> chapterCategoryCountMap) {
        if (chapterCategoryCountMap == null || chapterCategoryCountMap.isEmpty()) return "";

        return chapterCategoryCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .map(entry -> formatChapterCategoryLabel(entry.getKey()) + "(" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    /**
     * Formats one chapter category code with an optional Korean label.
     */
    private String formatChapterCategoryLabel(final String chapterCategory) {
        final String code = StringUtils.defaultString(chapterCategory);
        final String label = PERSON_CHAPTER_CATEGORY_LABELS.getOrDefault(code, code);
        if (StringUtils.equals(code, label)) return code;
        return label + "(" + code + ")";
    }

    /**
     * Holds linked context tag and chapter category counts for person-meaning aggregation.
     */
    private record PersonMeaningContextAggregates(
            Map<String, Integer> linkedContextTagCountMap,
            Map<String, Integer> chapterCategoryCountMap
    ) {}

    /**
     * Returns sources that carry person tags for person-meaning aggregation.
     */
    private List<RagSearchResult> resolvePersonFocusedResults(
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        return resolvePersonFocusedResults(results, personFocus, null);
    }

    /**
     * Returns sources that carry person tags for person-meaning aggregation.
     */
    private List<RagSearchResult> resolvePersonFocusedResults(
            final List<RagSearchResult> results,
            final PersonFocus personFocus,
            final String dominantTagStem
    ) {
        if (personFocus == null || results == null || results.isEmpty()) return List.of();

        final List<RagSearchResult> tagFocusedResults = results.stream()
                .filter(result -> !filterPersonMeaningTags(
                        extractSourceTags(result),
                        personFocus,
                        dominantTagStem
                ).isEmpty())
                .collect(Collectors.toList());
        if (!tagFocusedResults.isEmpty()) return tagFocusedResults;

        return List.of();
    }

    /**
     * person-meaning tag-only RAG 결과만으로 스냅샷을 집계하는지 확인합니다.
     *
     * <p>이 경우 entity catalog의 전역 role/kind/date 집계는 쓰지 않고 태그 매칭 source만 사용합니다.</p>
     */
    private boolean isTagOnlyPersonMeaningResults(final List<RagSearchResult> results) {
        if (results == null || results.isEmpty()) return false;
        for (final RagSearchResult result : results) {
            if (result == null) continue;
            if (!RagSearchResult.MATCH_TYPE_TAG.equals(result.getMatchType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * PERSON_MEANING_SCAFFOLD 재료를 답변용 스냅샷으로 집계합니다.
     */
    private PersonMeaningSnapshot buildPersonMeaningSnapshot(final RagContext ragContext) {
        return buildPersonMeaningSnapshot(ragContext, null);
    }

    /**
     * 질문 유형별 SNAPSHOT 예산으로 PERSON_MEANING_SCAFFOLD 재료를 집계합니다.
     *
     * <p>태도 질문은 근거 장면 건수·길이·총량을 키워 기록 본문을 더 많이 LLM에 전달합니다.</p>
     */
    private PersonMeaningSnapshot buildPersonMeaningSnapshot(
            final RagContext ragContext,
            final String queryText
    ) {
        final PersonMeaningSnapshotOptions options = resolvePersonMeaningSnapshotOptions(queryText);
        final PersonFocus personFocus = ragContext.personFocus();
        final List<RagSearchResult> results = ragContext.results() == null ? List.of() : ragContext.results();
        final String dominantTagStem = resolveDominantPersonTagStem(results, personFocus);
        final List<RagSearchResult> focusedResults = resolvePersonFocusedResults(results, personFocus, dominantTagStem);

        final Map<String, Integer> focusedTagCountMap = new LinkedHashMap<>();
        for (final RagSearchResult result : focusedResults) {
            incrementTagCounts(
                    focusedTagCountMap,
                    filterPersonMeaningTags(extractSourceTags(result), personFocus, dominantTagStem).stream()
                            .distinct()
                            .collect(Collectors.toList())
            );
        }

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                personFocus == null ? null : personFocus.entitySummary();
        final RagTimelineSummary timelineSummary = buildRagTimelineSummary(focusedResults);
        final boolean tagOnlyAggregation = isTagOnlyPersonMeaningResults(focusedResults);

        final Map<String, Integer> contentKindCountMap = new LinkedHashMap<>();
        if (!tagOnlyAggregation
                && entitySummary != null
                && entitySummary.contentKindCountMap() != null) {
            contentKindCountMap.putAll(entitySummary.contentKindCountMap());
        } else if (!timelineSummary.contentKindCountMap().isEmpty()) {
            contentKindCountMap.putAll(timelineSummary.contentKindCountMap());
        }

        final List<String> roleAxesKo = !tagOnlyAggregation && entitySummary != null
                ? formatPersonRoleAxes(entitySummary.roleCountMap())
                : List.of();

        final PersonMeaningContextAggregates contextAggregates =
                buildPersonMeaningContextAggregates(focusedResults, personFocus);

        final List<String> evidenceSnippets = new ArrayList<>();
        int evidenceChars = 0;
        final List<Integer> evidenceIndices = resolveEvidenceSourceIndices(
                focusedResults.size(),
                options.evidenceLimit(),
                options.spreadEvidenceAcrossSources()
        );
        for (final int evidenceIndex : evidenceIndices) {
            if (evidenceSnippets.size() >= options.evidenceLimit()) {
                break;
            }
            final String evidenceSnippet = sanitizePersonMeaningSnippet(
                    focusedResults.get(evidenceIndex),
                    personFocus,
                    options.snippetMaxLength()
            );
            if (StringUtils.isBlank(evidenceSnippet) || evidenceSnippets.contains(evidenceSnippet)) {
                continue;
            }
            if (evidenceChars + evidenceSnippet.length() > options.evidenceCharBudget()) {
                final int remaining = options.evidenceCharBudget() - evidenceChars;
                if (remaining < 40) {
                    continue;
                }
                final String trimmed = StringUtils.abbreviate(evidenceSnippet, remaining);
                if (StringUtils.isBlank(trimmed)) {
                    continue;
                }
                evidenceSnippets.add(trimmed);
                break;
            }
            evidenceSnippets.add(evidenceSnippet);
            evidenceChars += evidenceSnippet.length();
        }

        final String firstDate = !tagOnlyAggregation
                && entitySummary != null
                && StringUtils.isNotBlank(entitySummary.firstDate())
                ? entitySummary.firstDate()
                : timelineSummary.firstDate();
        final String lastDate = !tagOnlyAggregation
                && entitySummary != null
                && StringUtils.isNotBlank(entitySummary.lastDate())
                ? entitySummary.lastDate()
                : timelineSummary.lastDate();

        return new PersonMeaningSnapshot(
                focusedTagCountMap,
                roleAxesKo,
                contentKindCountMap,
                contextAggregates.linkedContextTagCountMap(),
                contextAggregates.chapterCategoryCountMap(),
                evidenceSnippets,
                firstDate,
                lastDate
        );
    }

    private static final int RULE_PRIMARY_EVIDENCE_MAX_LENGTH = 100;

    /**
     * person-meaning fallback용 근거 장면 텍스트를 정리합니다.
     *
     * <p>embedding_text 메타라인/HTML을 제거하고 본문 중심으로 축약합니다.</p>
     */
    private String sanitizePersonMeaningSnippet(final RagSearchResult result, final PersonFocus personFocus) {
        return sanitizePersonMeaningSnippet(result, personFocus, RULE_PRIMARY_EVIDENCE_MAX_LENGTH);
    }

    /**
     * SNAPSHOT 근거 장면 텍스트를 정리합니다.
     *
     * <p>태도 질문은 {@code maxLength}를 키워 인물 주변 본문을 더 넓게 잘라냅니다.</p>
     */
    private String sanitizePersonMeaningSnippet(
            final RagSearchResult result,
            final PersonFocus personFocus,
            final int maxLength
    ) {
        if (result == null || result.getEntity() == null) return "";

        String body = extractEmbeddingBodyText(result.getEntity().getEmbeddingText());
        if (StringUtils.isBlank(body)) {
            body = stripHtmlForPersonMeaning(StringUtils.defaultIfBlank(result.getSnippet(), ""));
        } else {
            body = stripHtmlForPersonMeaning(body);
        }
        body = StringUtils.normalizeSpace(body);
        if (StringUtils.isBlank(body)) return "";

        if (personFocus != null) {
            final List<String> tokenOrder = new ArrayList<>();
            if (StringUtils.isNotBlank(personFocus.primaryToken())) {
                tokenOrder.add(personFocus.primaryToken());
            }
            if (personFocus.tokens() != null) {
                for (final String token : personFocus.tokens()) {
                    if (StringUtils.isBlank(token) || tokenOrder.contains(token)) continue;
                    tokenOrder.add(token);
                }
            }
            for (final String token : tokenOrder) {
                if (StringUtils.isBlank(token)) continue;
                final int index = StringUtils.indexOfIgnoreCase(body, token);
                if (index < 0) continue;

                final int contextBefore = Math.max(40, maxLength / 3);
                final int contextAfter = Math.max(80, maxLength - contextBefore - 12);
                final int start = Math.max(0, index - contextBefore);
                final int end = Math.min(body.length(), index + contextAfter);
                final String prefix = start > 0 ? "..." : "";
                final String suffix = end < body.length() ? "..." : "";
                return StringUtils.abbreviate(
                        prefix + body.substring(start, end) + suffix,
                        maxLength
                );
            }
        }

        return StringUtils.abbreviate(body, maxLength);
    }

    /**
     * RULE_PRIMARY 근거 장면에서 대화 인용·화자 라벨을 줄여 한 줄 요약으로 만듭니다.
     *
     * <p>hybrid SNAPSHOT용 스냅샷 스니펫 원문은 변경하지 않습니다.</p>
     */
    private String compactRulePrimaryEvidenceSnippet(final String snippet) {
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
     * <p>질문 유형별 {@link PersonMeaningSnapshotOptions} 예산을 따릅니다. 태도 질문은 hybrid와 같이
     * 최대 {@link #PERSON_STANCE_SNAPSHOT_EVIDENCE_LIMIT}건·{@link #PERSON_STANCE_SNAPSHOT_SNIPPET_MAX_LENGTH}자,
     * 그 외는 {@link #PERSON_MEANING_SNAPSHOT_EVIDENCE_LIMIT}건·{@link #RULE_PRIMARY_EVIDENCE_MAX_LENGTH}자입니다.</p>
     */
    private void appendRulePrimaryEvidenceSection(final StringBuilder sb, final List<String> evidenceSnippets) {
        appendRulePrimaryEvidenceSection(sb, evidenceSnippets, PersonMeaningSnapshotOptions.defaults());
    }

    private void appendRulePrimaryEvidenceSection(
            final StringBuilder sb,
            final List<String> evidenceSnippets,
            final PersonMeaningSnapshotOptions options
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
                ? chatMsg("chat.ai.fallback.section.evidence-full")
                : chatMsg("chat.ai.fallback.section.evidence-compact");
        sb.append("\n").append(evidenceLabel).append(": ").append(compact);
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
     * person-meaning 해석 리드의 집계 본문(주어 없음)을 조립합니다.
     */
    private String buildPersonMeaningLeadBody(
            final Map<String, Integer> repeatedTagCountMap,
            final List<String> roleAxesKo,
            final Map<String, Integer> contentKindCountMap,
            final Map<String, Integer> linkedContextTagCountMap,
            final Map<String, Integer> chapterCategoryCountMap
    ) {
        final List<String> leadParts = new ArrayList<>();
        if (repeatedTagCountMap != null && !repeatedTagCountMap.isEmpty()) {
            leadParts.add(chatMsg("chat.ai.fallback.lead-body.repeated-axis", formatTopTagsForDisplay(repeatedTagCountMap, 3)));
        }
        if (linkedContextTagCountMap != null && !linkedContextTagCountMap.isEmpty()) {
            leadParts.add(chatMsg("chat.ai.fallback.lead-body.linked-tags", formatTopTagsForDisplay(linkedContextTagCountMap, 2)));
        }
        if (chapterCategoryCountMap != null && !chapterCategoryCountMap.isEmpty()) {
            leadParts.add(chatMsg("chat.ai.fallback.lead-body.chapter", formatChapterCategorySpread(chapterCategoryCountMap)));
        }
        if (roleAxesKo != null && !roleAxesKo.isEmpty()) {
            final String topRoleAxis = roleAxesKo.get(0);
            final int parenIndex = topRoleAxis.indexOf('(');
            final String roleLabel = parenIndex > 0 ? topRoleAxis.substring(0, parenIndex) : topRoleAxis;
            if (StringUtils.isNotBlank(roleLabel)) {
                leadParts.add(chatMsg("chat.ai.fallback.lead-body.role-axis", roleLabel));
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
            final Map<String, Integer> chapterCategoryCountMap
    ) {
        if (StringUtils.isBlank(target)) return "";

        final String leadBody = buildPersonMeaningLeadBody(
                repeatedTagCountMap,
                roleAxesKo,
                contentKindCountMap,
                linkedContextTagCountMap,
                chapterCategoryCountMap
        );
        if (StringUtils.isBlank(leadBody)) return "";
        return chatMsg("chat.ai.lead.person-meaning", target, leadBody);
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
            final Map<String, Integer> chapterCategoryCountMap
    ) {
        final String leadBody = buildPersonMeaningLeadBody(
                repeatedTagCountMap,
                roleAxesKo,
                contentKindCountMap,
                linkedContextTagCountMap,
                chapterCategoryCountMap
        );
        if (StringUtils.isBlank(leadBody)) return "";
        return chatMsg("chat.ai.lead.person-stance", leadBody);
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

        if (dreamCount > diaryCount && dreamCount > noteCount) return chatMsg("chat.ai.hint.content-kind.dream");
        if (diaryCount > dreamCount && diaryCount > noteCount) return chatMsg("chat.ai.hint.content-kind.diary");
        if (noteCount > dreamCount && noteCount > diaryCount) return chatMsg("chat.ai.hint.content-kind.note");
        if (dreamCount == diaryCount && dreamCount > 0) return chatMsg("chat.ai.hint.content-kind.dream-diary");
        return "";
    }

    /**
     * 모델이 빈 분류만 내놓았을 때 스캐폴드 데이터로 결정적 person-meaning 답변을 만듭니다.
     */
    private String buildPersonMeaningDeterministicFallback(final RagContext ragContext) {
        if (ragContext == null || ragContext.personFocus() == null) {
            return chatMsg("chat.ai.clarify.need-context");
        }
        if (ragContext.results() == null || ragContext.results().isEmpty()) {
            final String target = resolvePersonFocusTarget(ragContext.personFocus());
            return chatMsg("chat.ai.clarify.no-info-for-target", target);
        }

        final PersonFocus personFocus = ragContext.personFocus();
        final String target = resolvePersonFocusTarget(personFocus);
        final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext);
        final StringBuilder sb = new StringBuilder();

        final String interpretiveLead = buildPersonMeaningInterpretiveLead(
                target,
                snapshot.repeatedTagCountMap(),
                snapshot.roleAxesKo(),
                snapshot.contentKindCountMap(),
                snapshot.linkedContextTagCountMap(),
                snapshot.chapterCategoryCountMap()
        );
        if (StringUtils.isNotBlank(interpretiveLead)) {
            sb.append(interpretiveLead).append("\n\n");
        } else {
            sb.append(chatMsg("chat.ai.fallback.person-meaning.intro-no-lead", target)).append("\n\n");
        }

        if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.fallback.section.repeated-axis")).append(' ')
                    .append(formatTopTagsForDisplay(snapshot.repeatedTagCountMap(), 6)).append('\n');
            sb.append(chatMsg("chat.ai.fallback.person-meaning.repeated-axis-interpret", target)).append('\n');
        } else {
            sb.append(chatMsg("chat.ai.fallback.person-meaning.repeated-axis-empty", target)).append('\n');
        }

        if (!snapshot.linkedContextTagCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.fallback.section.linked-context")).append(' ')
                    .append(formatTopTagsForDisplay(snapshot.linkedContextTagCountMap(), 8))
                    .append('\n');
            sb.append(chatMsg("chat.ai.fallback.person-meaning.linked-context-interpret", target)).append('\n');
        }

        if (!snapshot.chapterCategoryCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.fallback.section.chapter-category")).append(' ')
                    .append(formatChapterCategorySpread(snapshot.chapterCategoryCountMap()))
                    .append('\n');
        }

        sb.append(chatMsg("chat.ai.fallback.section.role-function")).append(' ');
        if (!snapshot.roleAxesKo().isEmpty()) {
            sb.append(String.join(", ", snapshot.roleAxesKo())).append('\n');
        } else {
            appendPersonMeaningLinkedContextRoleHint(
                    sb,
                    snapshot.linkedContextTagCountMap(),
                    snapshot.chapterCategoryCountMap(),
                    false
            );
            sb.append('\n');
        }

        if (!snapshot.contentKindCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.fallback.section.content-kind")).append(' ')
                    .append(formatContentKindSpread(snapshot.contentKindCountMap())).append('\n');
        }

        if (StringUtils.isNotBlank(snapshot.firstDate()) || StringUtils.isNotBlank(snapshot.lastDate())) {
            sb.append(chatMsg(
                    "chat.ai.fallback.section.period",
                    StringUtils.defaultIfBlank(snapshot.firstDate(), "?"),
                    StringUtils.defaultIfBlank(snapshot.lastDate(), "?")
            )).append('\n');
        }

        appendRulePrimaryEvidenceSection(sb, snapshot.evidenceSnippets());

        sb.append('\n').append(chatMsg("chat.ai.fallback.person-meaning.uncertainty"));
        return sb.toString().trim();
    }

    /**
     * 대화/기록 속 인물 등장 방식 질문용 규칙 기반 응답을 만듭니다.
     */
    private String buildPersonAppearanceDeterministicFallback(final RagContext ragContext) {
        if (ragContext == null || ragContext.personFocus() == null) {
            return chatMsg("chat.ai.clarify.need-context");
        }
        if (ragContext.results() == null || ragContext.results().isEmpty()) {
            final String target = resolvePersonFocusTarget(ragContext.personFocus());
            return chatMsg("chat.ai.clarify.no-appearance-for-target", target);
        }

        final String target = resolvePersonFocusTarget(ragContext.personFocus());
        final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext);
        final StringBuilder sb = new StringBuilder();

        final String interpretiveLead = buildPersonAppearanceInterpretiveLead(
                target,
                snapshot.repeatedTagCountMap(),
                snapshot.roleAxesKo(),
                snapshot.contentKindCountMap(),
                snapshot.linkedContextTagCountMap(),
                snapshot.chapterCategoryCountMap()
        );
        if (StringUtils.isNotBlank(interpretiveLead)) {
            sb.append(interpretiveLead).append("\n\n");
        } else {
            sb.append(chatMsg("chat.ai.fallback.person-appearance.intro-no-lead", target)).append("\n\n");
        }

        sb.append(chatMsg("chat.ai.fallback.person-appearance.section-tone")).append(' ');
        if (!snapshot.roleAxesKo().isEmpty()) {
            sb.append(String.join(", ", snapshot.roleAxesKo()));
        } else if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.fallback.person-appearance.repeated-tags-prefix")).append(' ')
                    .append(formatTopTagsForDisplay(snapshot.repeatedTagCountMap(), 6));
        } else {
            sb.append(chatMsg("chat.ai.fallback.person-appearance.insufficient-axis"));
        }
        sb.append('\n');

        sb.append(chatMsg("chat.ai.fallback.person-appearance.section-repeat")).append(' ');
        boolean hasRepeatPattern = false;
        if (!snapshot.linkedContextTagCountMap().isEmpty()) {
            sb.append(chatMsg("chat.ai.fallback.person-appearance.linked-context-prefix")).append(' ')
                    .append(formatTopTagsForDisplay(snapshot.linkedContextTagCountMap(), 6));
            hasRepeatPattern = true;
        }
        if (!snapshot.chapterCategoryCountMap().isEmpty()) {
            if (hasRepeatPattern) sb.append("; ");
            sb.append(chatMsg("chat.ai.fallback.person-appearance.chapter-prefix")).append(' ')
                    .append(formatChapterCategorySpread(snapshot.chapterCategoryCountMap()));
            hasRepeatPattern = true;
        }
        if (!snapshot.contentKindCountMap().isEmpty()) {
            if (hasRepeatPattern) sb.append("; ");
            sb.append(chatMsg("chat.ai.fallback.person-appearance.content-kind-prefix")).append(' ')
                    .append(formatContentKindSpread(snapshot.contentKindCountMap()));
            hasRepeatPattern = true;
        }
        if (StringUtils.isNotBlank(snapshot.firstDate()) || StringUtils.isNotBlank(snapshot.lastDate())) {
            if (hasRepeatPattern) sb.append("; ");
            sb.append(chatMsg("chat.ai.fallback.person-appearance.period-prefix")).append(' ')
                    .append(StringUtils.defaultIfBlank(snapshot.firstDate(), "?"))
                    .append(" ~ ")
                    .append(StringUtils.defaultIfBlank(snapshot.lastDate(), "?"));
            hasRepeatPattern = true;
        }
        if (!hasRepeatPattern) {
            sb.append(chatMsg("chat.ai.fallback.person-appearance.insufficient-repeat"));
        }
        sb.append('\n');

        sb.append(chatMsg("chat.ai.fallback.person-appearance.section-bundled")).append(' ');
        if (!snapshot.repeatedTagCountMap().isEmpty()) {
            sb.append(formatTopTagsForDisplay(snapshot.repeatedTagCountMap(), 6));
        } else {
            sb.append(chatMsg("chat.ai.fallback.person-appearance.no-person-tags"));
        }
        sb.append('\n');

        sb.append(chatMsg("chat.ai.fallback.person-appearance.uncertainty")).append('\n');

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
            final Map<String, Integer> chapterCategoryCountMap
    ) {
        if (StringUtils.isBlank(target)) return "";

        final String leadBody = buildPersonMeaningLeadBody(
                repeatedTagCountMap,
                roleAxesKo,
                contentKindCountMap,
                linkedContextTagCountMap,
                chapterCategoryCountMap
        );
        if (StringUtils.isBlank(leadBody)) return "";
        return chatMsg("chat.ai.lead.person-appearance", target, leadBody);
    }
    /**
     * 태도 질문에서 모델이 성격 평가·코칭만 내놓았을 때 기록 근거로 2인칭 태도 답변을 만듭니다.
     */
    private String buildPersonStanceDeterministicFallback(final RagContext ragContext, final String queryText) {
        if (ragContext == null || ragContext.personFocus() == null) {
            return chatMsg("chat.ai.clarify.need-context");
        }
        if (ragContext.results() == null || ragContext.results().isEmpty()) {
            final String target = resolvePersonFocusTarget(ragContext.personFocus());
            return chatMsg("chat.ai.clarify.no-stance-for-target", target);
        }

        final PersonFocus personFocus = ragContext.personFocus();
        final String target = resolvePersonFocusTarget(personFocus);
        final PersonMeaningSnapshot snapshot = buildPersonMeaningSnapshot(ragContext, queryText);
        final StringBuilder sb = new StringBuilder();

        final String interpretiveLead = buildPersonStanceInterpretiveLead(
                target,
                snapshot.repeatedTagCountMap(),
                snapshot.roleAxesKo(),
                snapshot.contentKindCountMap(),
                snapshot.linkedContextTagCountMap(),
                snapshot.chapterCategoryCountMap()
        );
        if (StringUtils.isNotBlank(interpretiveLead)) {
            sb.append(interpretiveLead);
        } else {
            sb.append(chatMsg("chat.ai.fallback.person-stance.intro-no-lead", target));
        }
        sb.append(chatMsg("chat.ai.fallback.person-stance.caveat"));

        final List<String> stanceSnippets = snapshot.evidenceSnippets();
        if (stanceSnippets != null && !stanceSnippets.isEmpty()) {
            final int snippetLimit = Math.min(stanceSnippets.size(), 3);
            sb.append(chatMsg("chat.ai.fallback.person-stance.evidence-intro"))
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
        appendContentKindPart(parts, contentKindCountMap, "DREAM", chatMsg("chat.ai.fallback.content-kind.dream"));
        appendContentKindPart(parts, contentKindCountMap, "DIARY", chatMsg("chat.ai.fallback.content-kind.diary"));
        appendContentKindPart(parts, contentKindCountMap, "NOTE", chatMsg("chat.ai.fallback.content-kind.note"));
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
            return chatMsg("chat.ai.language-fallback.no-context");
        }
        if (ragContext.intent() == RagIntent.SYNTHESIS) {
            return chatMsg("chat.ai.language-fallback.synthesis-retry");
        }
        return chatMsg("chat.ai.language-fallback.lookup-retry");
    }

    /**
     * AI 응답에서 RAG 내부 기록 인덱스([1], [2] 등) 인용을 제거합니다.
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
     * person-meaning SNAPSHOT 집계·근거 예산.
     *
     * <p>태도 질문은 기록 본문을 더 많이 실어 사용자 정서 비춤을 돕습니다.</p>
     */
    private record PersonMeaningSnapshotOptions(
            int evidenceLimit,
            int snippetMaxLength,
            int evidenceCharBudget,
            boolean spreadEvidenceAcrossSources
    ) {
        private static PersonMeaningSnapshotOptions defaults() {
            return new PersonMeaningSnapshotOptions(
                    PERSON_MEANING_SNAPSHOT_EVIDENCE_LIMIT,
                    RULE_PRIMARY_EVIDENCE_MAX_LENGTH,
                    Integer.MAX_VALUE,
                    false
            );
        }

        private static PersonMeaningSnapshotOptions personStanceRich() {
            return new PersonMeaningSnapshotOptions(
                    PERSON_STANCE_SNAPSHOT_EVIDENCE_LIMIT,
                    PERSON_STANCE_SNAPSHOT_SNIPPET_MAX_LENGTH,
                    PERSON_STANCE_SNAPSHOT_EVIDENCE_CHAR_BUDGET,
                    true
            );
        }
    }

    /**
     * 질문 유형별 SNAPSHOT 근거 예산을 선택합니다.
     */
    private PersonMeaningSnapshotOptions resolvePersonMeaningSnapshotOptions(final String queryText) {
        if (isPersonAttitudeQuery(queryText)) {
            return PersonMeaningSnapshotOptions.personStanceRich();
        }
        return PersonMeaningSnapshotOptions.defaults();
    }

    /**
     * SNAPSHOT 근거 장면을 여러 source에 고르게 샘플링할 인덱스를 만듭니다.
     */
    private List<Integer> resolveEvidenceSourceIndices(
            final int sourceCount,
            final int evidenceLimit,
            final boolean spreadAcrossSources
    ) {
        if (sourceCount <= 0 || evidenceLimit <= 0) {
            return List.of();
        }
        final int pickCount = Math.min(sourceCount, evidenceLimit);
        if (!spreadAcrossSources || sourceCount <= pickCount) {
            return java.util.stream.IntStream.range(0, pickCount).boxed().collect(Collectors.toList());
        }

        final List<Integer> indices = new ArrayList<>(pickCount);
        for (int picked = 0; picked < pickCount; picked++) {
            final int index = (int) Math.round((double) picked * (sourceCount - 1) / Math.max(1, pickCount - 1));
            if (indices.isEmpty() || index > indices.get(indices.size() - 1)) {
                indices.add(index);
            }
        }
        for (int fallback = 0; indices.size() < pickCount && fallback < sourceCount; fallback++) {
            if (!indices.contains(fallback)) {
                indices.add(fallback);
            }
        }
        indices.sort(Integer::compareTo);
        return indices;
    }

    /**
     * person-meaning fallback 생성에 쓰는 스캐폴드 스냅샷.
     */
    private record PersonMeaningSnapshot(
            Map<String, Integer> repeatedTagCountMap,
            List<String> roleAxesKo,
            Map<String, Integer> contentKindCountMap,
            Map<String, Integer> linkedContextTagCountMap,
            Map<String, Integer> chapterCategoryCountMap,
            List<String> evidenceSnippets,
            String firstDate,
            String lastDate
    ) {}
    /**
     * LLM 경로에서 최종 본문과 responseMode를 함께 반환합니다.
     */
    private record ResolvedChatResponse(
            String content,
            String responseMode,
            String guardDetail,
            String retryGuardDetail
    ) {
        ResolvedChatResponse(final String content, final String responseMode) {
            this(content, responseMode, null, null);
        }

        ResolvedChatResponse(final String content, final String responseMode, final String guardDetail) {
            this(content, responseMode, guardDetail, null);
        }
    }

    /**
     * RAG 의도, 검색 결과, 프롬프트용 컨텍스트 텍스트를 함께 보관합니다.
     */
    private record RagContext(RagIntent intent, List<RagSearchResult> results, String text, PersonFocus personFocus) {
        private static RagContext empty(final RagIntent intent) {
            return new RagContext(intent, List.of(), null, null);
        }
    }

    /**
     * person-meaning 질문에서 우선 source를 정렬하는 데 쓰는 키워드 매칭 정보.
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
