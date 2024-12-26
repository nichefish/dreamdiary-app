package io.nicheblog.dreamdiary.domain.chat.service;

import io.nicheblog.dreamdiary.domain.chat.entity.ChatMsgEntity;
import io.nicheblog.dreamdiary.domain.chat.mapstruct.ChatMsgMapstruct;
import io.nicheblog.dreamdiary.domain.chat.model.ChatMsgDto;
import io.nicheblog.dreamdiary.domain.chat.repository.jpa.ChatMsgRepository;
import io.nicheblog.dreamdiary.domain.chat.spec.ChatMsgSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BasePostService;

/**\
 * ChatMsgService
 * <pre>
 *  채팅 메세지 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface ChatMsgService
        extends BasePostService<ChatMsgDto, ChatMsgDto, Integer, ChatMsgEntity, ChatMsgRepository, ChatMsgSpec, ChatMsgMapstruct> {
    //
}