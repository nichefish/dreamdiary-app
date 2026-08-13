package io.nicheblog.dreamdiary.feature.ai.model;

import lombok.Builder;
import lombok.Value;

/**
 * LLM chat API에 넘기는 역할·본문만 담은 메시지.
 *
 * <p>채팅 채널 DTO({@code ChatMessageDto})와 분리해 {@code feature.ai}가 {@code feature.chat}에
 * 의존하지 않도록 한다. role/content만 사용한다.</p>
 */
@Value
@Builder
public class AiChatMessage {

    /** 메시지 역할 (USER / ASSISTANT / SYSTEM 등, Ollama 정규화 전 내부값) */
    String role;

    /** 메시지 본문 */
    String content;
}
