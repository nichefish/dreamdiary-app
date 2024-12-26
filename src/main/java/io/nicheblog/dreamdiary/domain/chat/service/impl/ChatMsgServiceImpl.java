package io.nicheblog.dreamdiary.domain.chat.service.impl;

import io.nicheblog.dreamdiary.domain.chat.mapstruct.ChatMsgMapstruct;
import io.nicheblog.dreamdiary.domain.chat.repository.jpa.ChatMsgRepository;
import io.nicheblog.dreamdiary.domain.chat.service.ChatMsgService;
import io.nicheblog.dreamdiary.domain.chat.spec.ChatMsgSpec;
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
public class ChatMsgServiceImpl
        implements ChatMsgService {

    @Getter
    private final ChatMsgRepository repository;
    @Getter
    private final ChatMsgSpec spec;
    @Getter
    private final ChatMsgMapstruct mapstruct = ChatMsgMapstruct.INSTANCE;
}