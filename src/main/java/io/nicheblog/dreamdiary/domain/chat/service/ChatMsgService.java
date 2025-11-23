package io.nicheblog.dreamdiary.domain.chat.service;

import io.nicheblog.dreamdiary.domain.chat.entity.ChatMsgEntity;
import io.nicheblog.dreamdiary.domain.chat.mapstruct.ChatMsgMapstruct;
import io.nicheblog.dreamdiary.domain.chat.model.ChatMsgDto;
import io.nicheblog.dreamdiary.domain.chat.repository.jpa.ChatMsgRepository;
import io.nicheblog.dreamdiary.domain.chat.spec.ChatMsgSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BasePostService;
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
        implements BasePostService<ChatMsgDto, ChatMsgDto, Integer, ChatMsgEntity, ChatMsgRepository, ChatMsgSpec, ChatMsgMapstruct> {

    @Getter
    private final ChatMsgRepository repository;
    @Getter
    private final ChatMsgSpec spec;
    @Getter
    private final ChatMsgMapstruct mapstruct = ChatMsgMapstruct.INSTANCE;
}