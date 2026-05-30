package io.nicheblog.dreamdiary.feature.chat.model;

/**
 * 사용자 질문에 대한 RAG 검색/응답 의도.
 */
public enum RagIntent {
    /** 단일 키워드나 사실 확인 중심 */
    LOOKUP,
    /** 여러 기록을 묶어 요약 */
    SUMMARY,
    /** 반복 패턴, 변화, 상징, 감정선을 엮는 통섭 */
    SYNTHESIS
}
