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
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getSessionMessages(final Integer sessionId) throws Exception {
        return this.listEntityToDto(repository.findAllBySessionIdOrderBySeqAscCreatedAtAsc(sessionId));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getRecentContextMessages(final Integer sessionId) throws Exception {
        final List<ChatMessageEntity> entityList = repository.findTop20BySessionIdOrderBySeqDescCreatedAtDesc(sessionId);
        Collections.reverse(entityList);

        return this.listEntityToDto(entityList).stream()
                .filter(message -> message.getContent() != null && !message.getContent().trim().isEmpty())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Integer getNextSeq(final Integer sessionId) {
        return repository.findMaxSeqBySessionId(sessionId) + 1;
    }
}
