package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatMessageEntity;
import io.nicheblog.dreamdiary.feature.chat.mapstruct.ChatMessageMapstruct;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.chat.repository.jpa.ChatMessageRepository;
import io.nicheblog.dreamdiary.feature.chat.spec.ChatMessageSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * ChatMessageService
 * <pre>
 *  채팅 메세지 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatMessageService
        implements BaseAttachableService<ChatMessageDto, ChatMessageDto, Integer, ChatMessageEntity> {

    @Getter
    private final ChatMessageRepository repository;
    @Getter
    private final ChatMessageSpec spec;
    @Getter
    private final ChatMessageMapstruct mapstruct = ChatMessageMapstruct.INSTANCE;

    public ChatMessageMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public ChatMessageMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }
}
