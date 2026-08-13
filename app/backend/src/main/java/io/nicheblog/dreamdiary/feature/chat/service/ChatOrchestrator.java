package io.nicheblog.dreamdiary.feature.chat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.ai.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.ai.guard.ResponseGuardService;
import io.nicheblog.dreamdiary.feature.ai.prompt.IntentPromptResolver;
import io.nicheblog.dreamdiary.feature.ai.prompt.SystemPromptBuilder;
import io.nicheblog.dreamdiary.feature.ai.person.PersonFocus;
import io.nicheblog.dreamdiary.feature.ai.person.PersonFocusResolver;
import io.nicheblog.dreamdiary.feature.ai.person.PersonMeaningSnapshot;
import io.nicheblog.dreamdiary.feature.ai.person.PersonQueryClassifier;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSnapshotService;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSynthesisHybridService;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSynthesisResult;
import io.nicheblog.dreamdiary.feature.ai.model.AiChatMessage;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatSessionEntity;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContext;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContextService;
import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.feature.ai.rag.RagTagSummary;
import io.nicheblog.dreamdiary.feature.ai.rag.RagTimelineSummary;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchFacade;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchLimits;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import io.nicheblog.dreamdiary.feature.chat.ws.ChatWebSocketSender;
import org.springframework.stereotype.Service;


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
 * ChatOrchestrator
 * <pre>
 *  채팅 채널 오케스트레이터. 사용자 메시지 저장, RAG 컨텍스트 위임({@link RagContextService}), AI 응답 생성, WebSocket 브로드캐스트를 묶어 처리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatOrchestrator {

    /** RAG 검색에서 가져올 최대 저널 엔트리 수 (chat_setting 기본값) */
    private static final int RAG_TOP_K = 5;
    /** 요약형 RAG 검색에서 가져올 최대 저널 엔트리 수 */
    private static final int RAG_SUMMARY_TOP_K = 12;
    /** 통섭형 RAG 검색에서 가져올 최대 저널 엔트리 수 */
    private static final int RAG_SYNTHESIS_TOP_K = 25;
    /** LOOKUP/SUMMARY 벡터 기본 최소 점수 (관리자 {@code chat_setting.rag_min_score} 기본값). */
    private static final double RAG_MIN_SCORE = 0.35D;
    /** SYNTHESIS 벡터 기본 최소 점수 (관리자 {@code chat_setting.rag_synthesis_min_score} 기본값). */
    private static final double RAG_SYNTHESIS_MIN_SCORE = 0.25D;
    /** person-stance(태도) 질문 tag-only RAG에서 가져올 최대 저널 엔트리 수 */
    private static final int PERSON_STANCE_RAG_TOP_K = 50;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ChatSettingService chatSettingService;
    private final OllamaClient ollamaClient;
    private final RagSearchFacade ragSearchFacade;
    private final RagContextService ragContextService;
    private final PersonFocusResolver personFocusResolver;
    private final PersonSnapshotService personSnapshotService;
    private final PersonSynthesisHybridService personSynthesisHybridService;
    private final ResponseGuardService responseGuardService;
    private final SystemPromptBuilder systemPromptBuilder;
    private final IntentPromptResolver intentPromptResolver;
    private final ChatWebSocketSender chatWebSocketSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 세션별 응답 취소 플래그 */
    private final Map<Integer, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    /** chat.ai.* 카탈로그 메시지를 현재 locale 로 조회한다. */
    private String chatMsg(final String key, final Object... args) {
        return MessageUtils.getMessage(key, args);
    }

    /** 세션별 프롬프트보다 우선 적용할 응답 안전 규칙 (locale 카탈로그). */
    private String responseGuardPrompt() {
        return systemPromptBuilder.responseGuardPrompt();
    }

    /** LLM 이 언어 규칙을 어긴 경우 1회 재시도할 때 추가하는 지시문 (locale 카탈로그). */
    private String languageRetryPrompt() {
        return responseGuardService.languageRetryPrompt();
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
        chatWebSocketSender.broadcastMessage(
                sessionId,
                AjaxResponse.fromResponseWithObj(
                        userResult,
                        MessageUtils.getMessage("common.result.success")
                )
        );

        // 3. AI 응답 생성 (RAG 컨텍스트 주입)
        final int recentMessageLimit = chatSettingService.getMyRecentMessageLimit();
        chatWebSocketSender.broadcastProgress(sessionId, "SEARCHING");
        final RagContext ragContext = buildRagContext(message);

        if (isCancelled(sessionId)) {
            log.info("AI response cancelled. sessionId={}", sessionId);
            cancelFlags.remove(sessionId);
            return;
        }

        chatWebSocketSender.broadcastProgress(sessionId, "GENERATING");
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
            // 본경로만 스트리밍: 토큰 DELTA를 버블에 미리 보이고, 완성 후 글로벌 가드·저장·완성 broadcast는 기존과 동일.
            // hybrid/retry·language-guard 재시도는 비스트리밍 chat()을 유지한다.
            final AtomicBoolean cancelFlag = cancelFlags.get(sessionId);
            String rawResponse = ollamaClient.chatStream(
                    systemPrompt,
                    toAiChatMessages(contextMessages),
                    delta -> chatWebSocketSender.broadcastDelta(sessionId, delta),
                    cancelFlag
            );
            if (isCancelled(sessionId)) {
                log.info("AI response cancelled during stream. sessionId={}", sessionId);
                cancelFlags.remove(sessionId);
                return;
            }
            if (containsDisallowedHanScript(rawResponse)) {
                log.warn("AI response language guard retry. sessionId={}", sessionId);
                rawResponse = ollamaClient.chat(systemPrompt + languageRetryPrompt(), toAiChatMessages(contextMessages));
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
                    stripInternalRecordCitations(rawResponse)
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
        chatWebSocketSender.broadcastMessage(
                sessionId,
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
     * <p>본경로 스트림 중이면 NDJSON 읽기를 중단하고,
     * 완성 전이면 저장·완성 broadcast를 건너뛰다.
     * language-guard 재시도·hybrid 비스트림 호출은 HTTP가 끝날 때까지 대기한 뒤 플래그로 미저장한다.</p>
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
     * 사용자 메시지와 의미상 유사한 저널 기록을 검색해 RAG 컨텍스트를 조립한다.
     *
     * <p>검색·텍스트 조립은 {@link RagContextService}에 위임한다. 채널 관리자 RAG 설정만 여기서 매핑한다.</p>
     *
     * @param queryText 검색할 사용자 메시지
     * @return 의도·결과·텍스트·personFocus
     */
    private RagContext buildRagContext(final String queryText) {
        return ragContextService.build(
                queryText,
                toRagSearchLimits(resolveAdminRagSettings()),
                intentPromptResolver.intentClassifyPrompt()
        );
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
        return systemPromptBuilder.buildSystemPromptWithRag(
                basePrompt,
                ragContext == null ? null : ragContext.intent(),
                ragContext == null ? null : ragContext.text(),
                queryText
        );
    }

    /**
     * 인물 이름이 추출되고 '~에 대해 뭘 말해/알려줘' 류로 묻는 질문인지 확인합니다.
     */
    private boolean isPersonAboutLookupQuery(final String queryText) {
        return PersonQueryClassifier.isPersonAboutLookupQuery(queryText);
    }

    /**
     * 질문 문장을 보고 RAG 응답 의도를 분류한다.
     *
     * <p>인물 about 판정만 채널에서 수행하고, 휴리스틱·LLM 2차는 {@link RagSearchFacade#detectIntent}에 위임한다.</p>
     */
    private RagIntent detectRagIntent(final String queryText) {
        return ragContextService.detectIntent(
                queryText,
                intentPromptResolver.intentClassifyPrompt()
        );
    }

    /**
     * LLM 응답에서 LOOKUP/SUMMARY/SYNTHESIS 토큰을 추출한다. 없으면 null.
     *
     * <p>단위 테스트 호환용 위임. 정본은 {@link RagSearchFacade#parseIntentLabel(String)}.</p>
     */
    private RagIntent parseRagIntentLabel(final String raw) {
        return ragSearchFacade.parseIntentLabel(raw);
    }

    /**
     * RAG 의도·질문 유형별 검색 폭을 반환합니다.
     *
     * <p>태도 질문은 tag-only·merged 검색 모두에서 더 많은 인물 태그 기록을 가져옵니다.
     * (queryText 없이 intent만 받던 오버로드는 merged 경로가 queryText를 넘기게 되며 제거됨.)</p>
     */
    private int resolveRagTopK(final RagIntent intent, final String queryText) {
        return ragContextService.resolveTopK(intent, queryText, toRagSearchLimits(resolveAdminRagSettings()));
    }

    /**
     * RAG 의도별 벡터 검색 최소 점수를 반환합니다.
     */
    private double resolveRagMinScore(final RagIntent intent) {
        return ragContextService.resolveMinScore(intent, toRagSearchLimits(resolveAdminRagSettings()));
    }

    /**
     * 관리자 RAG 설정을 조회한다. 단위 테스트에서 {@code chatSettingService} null일 때 코드 기본값을 쓰다.
     */
    private ChatSettingService.RagAdminSettings resolveAdminRagSettings() {
        if (chatSettingService == null) {
            return new ChatSettingService.RagAdminSettings(
                    true,
                    RAG_TOP_K,
                    RAG_MIN_SCORE,
                    RAG_SUMMARY_TOP_K,
                    RAG_SYNTHESIS_TOP_K,
                    PERSON_STANCE_RAG_TOP_K,
                    RAG_SYNTHESIS_MIN_SCORE
            );
        }
        return chatSettingService.getAdminRagSettings();
    }

    /**
     * 채널 관리자 RAG 설정을 {@link RagSearchLimits}로 매핑한다.
     *
     * @param settings chat_setting 기반 관리자 값
     * @return ai/rag 검색 한도
     */
    private RagSearchLimits toRagSearchLimits(final ChatSettingService.RagAdminSettings settings) {
        return new RagSearchLimits(
                settings.enabled(),
                settings.topK(),
                settings.minScore(),
                settings.summaryTopK(),
                settings.synthesisTopK(),
                settings.stanceTopK(),
                settings.synthesisMinScore()
        );
    }

    /**
     * entity catalog 역할 축을 한국어 해석 라벨 목록으로 변환합니다.
     */
    private List<String> formatPersonRoleAxes(final Map<JournalEntityRoleType, Integer> roleCountMap) {
        return personSnapshotService.formatPersonRoleAxes(roleCountMap);
    }

    /**
     * 단일 역할 축을 빈도와 함께 한국어 라벨로 포맷합니다.
     */
    private String formatPersonRoleAxis(final JournalEntityRoleType roleType, final int count) {
        return personSnapshotService.formatPersonRoleAxis(roleType, count);
    }

    /**
     * 통섭 인물 질문에서 여러 source에 반복되는 인물 축을 좁혀야 할 때 person focus를 구성합니다.
     */
    private PersonFocus resolvePersonFocus(
            final String queryText,
            final RagIntent intent,
            final List<RagSearchResult> results
    ) {
        return personFocusResolver.resolvePersonFocus(queryText, intent, results);
    }

    /**
     * query가 person-centric synthesis 질문인지 확인합니다.
     */
    private boolean isPersonMeaningQuery(final String queryText) {
        return PersonQueryClassifier.isPersonMeaningQuery(queryText);
    }

    /**
     * 1인칭 태도·자기인식 질문(나는 X를 어떻게 생각/느끼는지)인지 확인합니다.
     *
     * <p>person-meaning(상징·역할 축·등장 방식)과 구분해 Path C 태도 rich-trust 프롬프트·최소 게이트를 태웁니다.
     * (예전에는 PERSON_STANCE_SCAFFOLD·강경 가드를 태웠으나 Option A 수렴으로 제거됨.)
     * {@code 내 대화에서 X는 어떤 느낌으로 등장}처럼 범위+등장 질문은 false입니다.</p>
     */
    private boolean isPersonAttitudeQuery(final String queryText) {
        return PersonQueryClassifier.isPersonAttitudeQuery(queryText);
    }

    /**
     * 기록·대화 속 인물의 등장 방식·느낌·톤을 묻는 질문인지 확인합니다.
     *
     * <p>1인칭 태도 질문과 달리 주어가 인물({@code 지연님은 … 등장})이거나 {@code 내 대화/내 기록} 범위 질문입니다.</p>
     */
    private boolean isPersonAppearanceQuery(final String queryText) {
        return PersonQueryClassifier.isPersonAppearanceQuery(queryText);
    }

    /**
     * {@code 나는/내가} 등 주어가 사용자 자신인 1인칭 표지가 있는지 확인합니다.
     *
     * <p>{@code 내 대화}·{@code 내 기록} 같은 소유 범위만으로는 true가 되지 않습니다.</p>
     */
    private boolean hasExplicitFirstPersonSubjectMarker(final String queryText) {
        return PersonQueryClassifier.hasExplicitFirstPersonSubjectMarker(queryText);
    }

    /**
     * person focus로 삼을 후보 token을 추출합니다.
     */
    private List<String> extractPersonFocusTokens(final String queryText) {
        return PersonQueryClassifier.extractPersonFocusTokens(queryText);
    }

    /**
     * Merge query person tokens with canonical entity labels already normalized by the entity catalog.
     */
    private List<String> mergePersonFocusTokens(
            final List<String> tokens,
            final JournalEntityFocusService.PersonEntityFocusSummary entitySummary
    ) {
        return personFocusResolver.mergePersonFocusTokens(tokens, entitySummary);
    }

    /**
     * Prefer the catalog canonical label as the primary focus target once the entity summary exists.
     */
    private String resolvePersonFocusPrimaryToken(
            final String queryText,
            final List<String> tokens,
            final JournalEntityFocusService.PersonEntityFocusSummary entitySummary
    ) {
        return personFocusResolver.resolvePersonFocusPrimaryToken(queryText, tokens, entitySummary);
    }

    /**
     * 질문 문장에서 person focus 대상 이름 토큰을 고릅니다.
     *
     * <p>{@code 지연님}처럼 호칭이 붙은 토큰을 우선하고, {@code 대화} 같은 범위어는 건너뜁니다.</p>
     */
    private String selectPrimaryPersonTokenFromQuery(final String queryText, final List<String> tokens) {
        return personFocusResolver.selectPrimaryPersonTokenFromQuery(queryText, tokens);
    }

    /**
     * person focus 관련성이 높은 source를 우선 근거로 쓰도록 정렬합니다.
     */
    private List<RagSearchResult> prioritizeResultsForPersonFocus(
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        return personFocusResolver.prioritizeResultsForPersonFocus(results, personFocus);
    }

    /**
     * person-meaning RAG 정렬 우선순위: TAG > ENTITY > person 태그 > 본문 언급.
     */
    private int resolvePersonFocusMatchPriority(final RagSearchResult result, final PersonFocus personFocus) {
        return personFocusResolver.resolvePersonFocusMatchPriority(result, personFocus);
    }

    /**
     * source payload 태그에 person focus 토큰이 포함되어 있는지 확인합니다.
     */
    private boolean hasPersonTagMatch(final RagSearchResult result, final PersonFocus personFocus) {
        return personFocusResolver.hasPersonTagMatch(result, personFocus);
    }

    /**
     * source가 person focus 토큰을 포함하는지 확인합니다.
     */
    private boolean mentionsPersonFocus(final RagSearchResult result, final PersonFocus personFocus) {
        return personFocusResolver.mentionsPersonFocus(result, personFocus);
    }

    /**
     * Resolve the display target for one PERSON_FOCUS block.
     */
    private String resolvePersonFocusTarget(final PersonFocus personFocus) {
        return personFocusResolver.resolvePersonFocusTarget(personFocus);
    }

    /**
     * source 텍스트가 person token을 포함하는지 확인합니다.
     */
    private boolean mentionsPersonTokens(final RagSearchResult result, final List<String> tokens) {
        return personFocusResolver.mentionsPersonTokens(result, tokens);
    }

    /**
     * KEYWORD 매칭 source에서 person token이 직접 매칭되는 횟수를 계산합니다.
     */
    private Integer countDirectPersonTokenMatches(final RagSearchResult result, final PersonFocus personFocus) {
        return personFocusResolver.countDirectPersonTokenMatches(result, personFocus);
    }

    /**
     * person focus source 매칭에 사용할 보조 텍스트를 수집합니다.
     */
    private String buildPersonFocusSourceText(final RagSearchResult result) {
        return personFocusResolver.buildPersonFocusSourceText(result);
    }

    /**
     * source 텍스트를 person focus 검색용 문자열에 추가합니다.
     */
    private void appendSourcePart(final StringBuilder sb, final Object value) {
        personFocusResolver.appendSourcePart(sb, value);
    }

    /**
     * person 토큰이 로그/기록 텍스트에서도 확인 가능한 리터럴인지 확인합니다.
     */
    private boolean containsReadablePersonToken(final String token) {
        return PersonQueryClassifier.containsReadablePersonToken(token);
    }

    /**
     * 흔한 조사를 제거해 해석 대상 이름의 토큰으로 정규화합니다.
     */
    private String stripTrailingJosa(final String token) {
        return PersonQueryClassifier.stripTrailingJosa(token);
    }

    /**
     * SYNTHESIS person 질문은 스냅샷 hybrid(LLM 해석 + rule-primary 폴백) 경로를 탑니다.
     */
    private boolean shouldUseRulePrimaryPersonSynthesisResponse(final RagContext ragContext, final String queryText) {
        if (ragContext == null) return false;
        return personSynthesisHybridService.shouldUsePathC(
                ragContext.personFocus(),
                ragContext.intent(),
                queryText
        );
    }

    /**
     * person SYNTHESIS 질문 유형에 맞는 규칙 기반 응답을 조립합니다.
     */
    private String buildRulePrimaryPersonSynthesisResponse(final RagContext ragContext, final String queryText) {
        if (ragContext == null) {
            return personSynthesisHybridService.buildRulePrimaryPersonSynthesisResponse(null, List.of(), queryText);
        }
        return personSynthesisHybridService.buildRulePrimaryPersonSynthesisResponse(
                ragContext.personFocus(),
                ragContext.results(),
                queryText
        );
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
        final List<ChatMessageDto> hybridContext = buildPersonSynthesisHybridContext(
                sessionId,
                message,
                recentMessageLimit
        );
        final PersonFocus personFocus = ragContext == null ? null : ragContext.personFocus();
        final List<RagSearchResult> results = ragContext == null || ragContext.results() == null
                ? List.of()
                : ragContext.results();
        final RagIntent intent = ragContext == null ? null : ragContext.intent();
        final PersonSynthesisResult result = personSynthesisHybridService.resolveHybrid(
                sessionId,
                message,
                intent,
                personFocus,
                results,
                toAiChatMessages(hybridContext),
                responseGuardService
        );
        return toResolvedChatResponse(result);
    }

    /**
     * {@link PersonSynthesisResult}를 채널 내부 {@link ResolvedChatResponse}로 매핑합니다.
     */
    private ResolvedChatResponse toResolvedChatResponse(final PersonSynthesisResult result) {
        if (result == null) {
            return new ResolvedChatResponse("", "RULE_PRIMARY", "empty_response");
        }
        return new ResolvedChatResponse(
                result.content(),
                result.responseMode(),
                result.guardDetail(),
                result.retryGuardDetail()
        );
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
     */
    private String buildPersonSynthesisHybridSystemPrompt(final RagContext ragContext, final String queryText) {
        if (ragContext == null) {
            return personSynthesisHybridService.buildHybridSystemPrompt(null, List.of(), queryText);
        }
        return personSynthesisHybridService.buildHybridSystemPrompt(
                ragContext.personFocus(),
                ragContext.results() == null ? List.of() : ragContext.results(),
                queryText
        );
    }



    /**
     * 채널 {@link ChatMessageDto} 목록을 LLM 입력용 {@link AiChatMessage}로 변환한다.
     *
     * <p>{@code feature.ai}는 chat DTO에 의존하지 않으므로, 오케스트레이터에서 role/content만 넘긴다.</p>
     *
     * @param messages 세션 맥락 메시지 (null 허용)
     * @return AI 클라이언트에 전달할 메시지 목록
     */
    private List<AiChatMessage> toAiChatMessages(final List<ChatMessageDto> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        final List<AiChatMessage> out = new ArrayList<>(messages.size());
        for (final ChatMessageDto message : messages) {
            if (message == null) {
                continue;
            }
            out.add(AiChatMessage.builder()
                    .role(message.getRole())
                    .content(message.getContent())
                    .build());
        }
        return out;
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
        final String retryResponse = stripInternalRecordCitations(ollamaClient.chat(
                systemPrompt + buildPersonMeaningRetryPrompt(ragContext, message, firstGuardDetail),
                toAiChatMessages(contextMessages)
        ));
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
     * 사용자 응답용 태그 표시 문자열에서 [엔서클] 등 메타 접두를 제거합니다.
     */
    private String formatDisplayTag(final String rawTag) {
        return personFocusResolver.formatDisplayTag(rawTag);
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
        final RagTagSummary tagSummary = ragContextService.buildTagSummary(results, personFocus);
        final Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("totalTags", buildTopTagCountMetadata(tagSummary.totalTagCountMap(), 20));
        metadata.put("dreamTags", buildTopTagCountMetadata(tagSummary.dreamTagCountMap(), 20));
        metadata.put("diaryTags", buildTopTagCountMetadata(tagSummary.diaryTagCountMap(), 20));
        metadata.put("noteTags", buildTopTagCountMetadata(tagSummary.noteTagCountMap(), 20));
        metadata.put("tagPairs", buildTopTagCountMetadata(tagSummary.tagPairCountMap(), 20));
        return metadata;
    }

    /**
     * 메시지 metadata에 저장할 시간/유형 요약 정보를 구성합니다.
     */
    private Map<String, Object> buildTimelineSummaryMetadata(final List<RagSearchResult> results) {
        final RagTimelineSummary timelineSummary = ragContextService.buildTimelineSummary(results);
        final Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sourceCount", timelineSummary.sourceCount());
        metadata.put("firstDate", timelineSummary.firstDate());
        metadata.put("lastDate", timelineSummary.lastDate());
        metadata.put("contentKinds", buildTopTagCountMetadata(timelineSummary.contentKindCountMap(), 10));
        metadata.put("months", buildTopTagCountMetadata(timelineSummary.monthCountMap(), 24));
        return metadata;
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
    /**
     * RAG 의도별 추가 프롬프트를 구성합니다. 정본은 {@link IntentPromptResolver#buildIntentPrompt}.
     */
    private String buildIntentPrompt(final RagIntent intent, final String queryText) {
        return intentPromptResolver.buildIntentPrompt(intent, queryText);
    }

    private boolean isPersonLookupQuery(final String queryText) {
        return PersonQueryClassifier.isPersonLookupQuery(queryText);
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
        return personFocusResolver.extractSourceTags(result);
    }

    /**
     * 임베딩 payload JSON을 Map으로 변환합니다.
     */
    private Map<String, Object> readEmbeddingPayload(final RagSearchResult result) {
        return personFocusResolver.readEmbeddingPayload(result);
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
        return responseGuardService.containsDisallowedHanScript(text);
    }

    /**
     * person-meaning 통섭 답변이 빈 분류·스캐폴드 유출·무관 태그 인용만 있는지 검사합니다.
     */
    private boolean isHollowPersonMeaningResponse(
            final String response,
            final RagContext ragContext,
            final String queryText
    ) {
        return responseGuardService.isHollowPersonMeaningResponse(
                response,
                ragContext == null ? null : ragContext.personFocus(),
                ragContext == null || ragContext.results() == null ? List.of() : ragContext.results(),
                ragContext == null ? null : ragContext.intent(),
                queryText
        );
    }

    /**
     * person 가드 실패 시 UI/로그용 짧은 사유 코드를 반환합니다.
     */
    private String describePersonGuardFailure(
            final String response,
            final RagContext ragContext,
            final String queryText
    ) {
        return responseGuardService.describePersonGuardFailure(
                response,
                ragContext == null ? null : ragContext.personFocus(),
                ragContext == null || ragContext.results() == null ? List.of() : ragContext.results(),
                ragContext == null ? null : ragContext.intent(),
                queryText
        );
    }

    /**
     * SYNTHESIS person-meaning hollow guard와 LOOKUP 인물 태도 질문의 빈 주제 분류를 함께 검사합니다.
     */
    private boolean isDegradedPersonResponse(
            final String response,
            final RagContext ragContext,
            final String queryText
    ) {
        return responseGuardService.isDegradedPersonResponse(
                response,
                ragContext == null ? null : ragContext.personFocus(),
                ragContext == null || ragContext.results() == null ? List.of() : ragContext.results(),
                ragContext == null ? null : ragContext.intent(),
                queryText
        );
    }

    /**
     * 태도 질문 "풍부 신뢰" 모드(Option A)의 최소 거부 게이트.
     *
     * <p>형식(4섹션)·코칭/조직 톤·3인칭 서술·에피소드 나열·인용은 더 이상 거부 사유가 아니다.
     * 큰 SNAPSHOT을 읽고 자연스러운 산문으로 태도를 서술하도록 허용하며, 빈 응답·너무 짧은 답·
     * 스캐폴드 유출·기록 근거 없는 일반 버킷 나열만 거부한다. 한글 언어 가드는 상위 흐름에서 별도 처리한다.</p>
     */
    private boolean isDegradedPersonStanceRichResponse(final String response) {
        return responseGuardService.isDegradedPersonStanceRichResponse(response);
    }

    /**
     * 태도 질문 "풍부 신뢰" 모드에서 거부 사유 코드를 반환한다.
     *
     * <p>{@link #isDegradedPersonStanceRichResponse(String)}와 동일한 판정을 코드로 표현한다.</p>
     */
    private String describePersonStanceRichGuardFailure(final String response) {
        return responseGuardService.describePersonStanceRichGuardFailure(response);
    }

    /**
     * person-meaning 답변이 태그 전체 문자열 또는 # 후 핵심어를 인용했는지 확인합니다.
     */
    private boolean citesPersonMeaningTagEvidence(final String response, final Map<String, Integer> tagCountMap) {
        return responseGuardService.citesPersonMeaningTagEvidence(response, tagCountMap);
    }

    /**
     * person 태그·역할 축이 없을 때 근거 스니펫 인용 여부를 확인합니다.
     */
    private boolean citesPersonMeaningSnippetEvidence(final String response, final List<String> evidenceSnippets) {
        return responseGuardService.citesPersonMeaningSnippetEvidence(response, evidenceSnippets);
    }

    /**
     * Builds a one-shot retry prompt when person-meaning hollow guard rejects the first LLM answer.
     */
    private String buildPersonMeaningRetryPrompt(
            final RagContext ragContext,
            final String queryText,
            final String guardDetail
    ) {
        final PersonFocus personFocus = ragContext == null ? null : ragContext.personFocus();
        final List<RagSearchResult> results = ragContext == null || ragContext.results() == null
                ? List.of()
                : ragContext.results();
        return personSynthesisHybridService.buildPersonMeaningRetryPrompt(
                personFocus, results, queryText, guardDetail
        );
    }


    /**
     * person-meaning 해석에 쓸 인물 태그만 남깁니다.
     *
     * <p>인물 축 태그에는 canonical/surface 이름(예: 김민수)이 포함된다는 Dreamdiary 태그 계약을 따릅니다.</p>
     */
    private List<String> filterPersonMeaningTags(final List<String> tags, final PersonFocus personFocus) {
        return personFocusResolver.filterPersonMeaningTags(tags, personFocus);
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
        return personFocusResolver.filterPersonMeaningTags(tags, personFocus, dominantTagStem);
    }

    /**
     * 태그 문자열에 person focus 토큰이 포함되어 있는지 확인합니다.
     */
    private boolean isPersonRelevantTag(final String tag, final PersonFocus personFocus) {
        return personFocusResolver.isPersonRelevantTag(tag, personFocus);
    }

    /**
     * 태그 문자열이 person focus(및 선택적 dominant stem)와 맞는지 확인합니다.
     */
    private boolean isPersonRelevantTag(
            final String tag,
            final PersonFocus personFocus,
            final String dominantTagStem
    ) {
        return personFocusResolver.isPersonRelevantTag(tag, personFocus, dominantTagStem);
    }

    /**
     * person 태그에서 # stem만 추출합니다.
     */
    private String extractPersonTagStem(final String tag) {
        return personFocusResolver.extractPersonTagStem(tag);
    }

    /**
     * 짧은 person 토큰(예: 지연)에 대해 반복 빈도가 가장 높은 태그 stem을 고릅니다.
     */
    private String resolveDominantPersonTagStem(
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        return personFocusResolver.resolveDominantPersonTagStem(results, personFocus);
    }

    /**
     * Keeps co-occurring scene tags from person-meaning sources while excluding person-focus tags.
     */
    private List<String> filterPersonMeaningLinkedContextTags(final List<String> tags, final PersonFocus personFocus) {
        return personFocusResolver.filterPersonMeaningLinkedContextTags(tags, personFocus);
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
     * Aggregates linked context tags and chapter Prefix values for person-meaning fallback/scaffold.
     */
    private PersonMeaningContextAggregates buildPersonMeaningContextAggregates(
            final List<RagSearchResult> focusedResults,
            final PersonFocus personFocus
    ) {
        final Map<String, Integer> linkedContextTagCountMap = new LinkedHashMap<>();
        final Map<String, Integer> chapterPrefixCountMap = new LinkedHashMap<>();
        if (focusedResults == null || focusedResults.isEmpty()) {
            return new PersonMeaningContextAggregates(linkedContextTagCountMap, chapterPrefixCountMap);
        }

        for (final RagSearchResult result : focusedResults) {
            if (result == null) continue;
            incrementTagCounts(
                    linkedContextTagCountMap,
                    filterPersonMeaningLinkedContextTags(extractSourceTags(result), personFocus)
            );
            final String chapterPrefix = extractSourceChapterPrefix(result);
            if (StringUtils.isNotBlank(chapterPrefix)) {
                chapterPrefixCountMap.merge(chapterPrefix, 1, Integer::sum);
            }
        }
        return new PersonMeaningContextAggregates(linkedContextTagCountMap, chapterPrefixCountMap);
    }

    /**
     * Reads the selected chapter Prefix name from a RAG source embedding payload.
     */
    private String extractSourceChapterPrefix(final RagSearchResult result) {
        if (result == null || result.getEntity() == null) return "";
        final Map<String, Object> payload = readEmbeddingPayload(result);
        return StringUtils.trimToEmpty(String.valueOf(payload.getOrDefault("journalChapterPrefixName", "")));
    }



    /**
     * Holds linked context tag and chapter Prefix counts for person-meaning aggregation.
     */
    private record PersonMeaningContextAggregates(
            Map<String, Integer> linkedContextTagCountMap,
            Map<String, Integer> chapterPrefixCountMap
    ) {}

    /**
     * Returns sources that carry person tags for person-meaning aggregation.
     */
    private List<RagSearchResult> resolvePersonFocusedResults(
            final List<RagSearchResult> results,
            final PersonFocus personFocus
    ) {
        return personSnapshotService.resolvePersonFocusedResults(results, personFocus);
    }

    /**
     * Returns sources that carry person tags for person-meaning aggregation.
     */
    private List<RagSearchResult> resolvePersonFocusedResults(
            final List<RagSearchResult> results,
            final PersonFocus personFocus,
            final String dominantTagStem
    ) {
        return personSnapshotService.resolvePersonFocusedResults(results, personFocus, dominantTagStem);
    }

    /**
     * person-meaning tag-only RAG 결과만으로 스냅샷을 집계하는지 확인합니다.
     *
     * <p>이 경우 entity catalog의 전역 role/kind/date 집계는 쓰지 않고 태그 매칭 source만 사용합니다.</p>
     */
    private boolean isTagOnlyPersonMeaningResults(final List<RagSearchResult> results) {
        return personSnapshotService.isTagOnlyPersonMeaningResults(results);
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
        if (ragContext == null) {
            return personSnapshotService.build(List.of(), null, queryText);
        }
        return personSnapshotService.build(ragContext.results(), ragContext.personFocus(), queryText);
    }


    /**
     * person-meaning fallback용 근거 장면 텍스트를 정리합니다.
     *
     * <p>embedding_text 메타라인/HTML을 제거하고 본문 중심으로 축약합니다.</p>
     */
    private String sanitizePersonMeaningSnippet(final RagSearchResult result, final PersonFocus personFocus) {
        return personSnapshotService.sanitizePersonMeaningSnippet(result, personFocus);
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
        return personSnapshotService.sanitizePersonMeaningSnippet(result, personFocus, maxLength);
    }









    /**
     * 모델이 빈 분류만 내놓았을 때 스캐폴드 데이터로 결정적 person-meaning 답변을 만듭니다.
     */
    private String buildPersonMeaningDeterministicFallback(final RagContext ragContext) {
        if (ragContext == null) {
            return personSynthesisHybridService.buildPersonMeaningDeterministicFallback(null, List.of());
        }
        return personSynthesisHybridService.buildPersonMeaningDeterministicFallback(
                ragContext.personFocus(),
                ragContext.results() == null ? List.of() : ragContext.results()
        );
    }

    /**
     * 대화/기록 속 인물 등장 방식 질문용 규칙 기반 응답을 만듭니다.
     */
    private String buildPersonAppearanceDeterministicFallback(final RagContext ragContext) {
        if (ragContext == null) {
            return personSynthesisHybridService.buildPersonAppearanceDeterministicFallback(null, List.of());
        }
        return personSynthesisHybridService.buildPersonAppearanceDeterministicFallback(
                ragContext.personFocus(),
                ragContext.results() == null ? List.of() : ragContext.results()
        );
    }

    /**
     * 태도 질문에서 모델이 성격 평가·코칭만 내놓았을 때 기록 근거로 2인칭 태도 답변을 만듭니다.
     */
    private String buildPersonStanceDeterministicFallback(final RagContext ragContext, final String queryText) {
        if (ragContext == null) {
            return personSynthesisHybridService.buildPersonStanceDeterministicFallback(null, List.of(), queryText);
        }
        return personSynthesisHybridService.buildPersonStanceDeterministicFallback(
                ragContext.personFocus(),
                ragContext.results() == null ? List.of() : ragContext.results(),
                queryText
        );
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
     * RULE_PRIMARY fallback 답변에 근거 장면을 덧붙입니다.
     */
    private void appendRulePrimaryEvidenceSection(final StringBuilder sb, final List<String> evidenceSnippets) {
        personSynthesisHybridService.appendRulePrimaryEvidenceSection(sb, evidenceSnippets);
    }

    /**
     * RULE_PRIMARY 근거 장면에서 대화 인용·화자 라벨을 줄여 한 줄 요약으로 만듭니다.
     */
    private String compactRulePrimaryEvidenceSnippet(final String snippet) {
        return personSynthesisHybridService.compactRulePrimaryEvidenceSnippet(snippet);
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
     * 질문 유형별 SNAPSHOT 근거 예산을 선택합니다.
     */
    private PersonSnapshotService.PersonMeaningSnapshotOptions resolvePersonMeaningSnapshotOptions(final String queryText) {
        return personSnapshotService.resolvePersonMeaningSnapshotOptions(queryText);
    }

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

}
