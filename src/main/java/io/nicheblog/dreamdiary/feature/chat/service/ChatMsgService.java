package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatMsgEntity;
import io.nicheblog.dreamdiary.feature.chat.mapstruct.ChatMsgMapstruct;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMsgDto;
import io.nicheblog.dreamdiary.feature.chat.repository.jpa.ChatMsgRepository;
import io.nicheblog.dreamdiary.feature.chat.spec.ChatMsgSpec;
import io.nicheblog.dreamdiary.feature.clsf.shared.service.BaseClsfService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**\
 * ChatMsgService
 * <pre>
 *  채팅 메세지 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("chatMsgService")
@RequiredArgsConstructor
@Log4j2
public class ChatMsgService
        implements BaseClsfService<ChatMsgDto, ChatMsgDto, Integer, ChatMsgEntity> {

    @Getter
    private final ChatMsgRepository repository;
    @Getter
    private final ChatMsgSpec spec;
    @Getter
    private final ChatMsgMapstruct mapstruct = ChatMsgMapstruct.INSTANCE;

    public ChatMsgMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public ChatMsgMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }
}
