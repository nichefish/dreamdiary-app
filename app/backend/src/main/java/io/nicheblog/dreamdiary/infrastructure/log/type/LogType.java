package io.nicheblog.dreamdiary.infrastructure.log.type;

/**
 * 로그 타입
 *
 * @author nichefish
 */
public enum LogType {
    PAGE,
    VIEW,
    ACTION,
    /** 시스템/배치 등 HTTP 컨텍스트 없는 로그 */
    SYSTEM
}
