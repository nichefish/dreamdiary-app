package io.nicheblog.dreamdiary.feature.ai.person;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;

import java.util.List;

/**
 * person-meaning 질문에서 우선 source를 정렬하는 데 쓰는 키워드 매칭 정보.
 *
 * @param primaryToken 대표 인물 토큰(카탈로그 canonical 우선)
 * @param tokens 질의·카탈로그 surface를 합친 focus 토큰
 * @param matchedSourceCount focus 토큰이 언급된 RAG source 수
 * @param entitySummary entity catalog 인물 요약(없으면 null)
 */
public record PersonFocus(
        String primaryToken,
        List<String> tokens,
        int matchedSourceCount,
        JournalEntityFocusService.PersonEntityFocusSummary entitySummary
) {}