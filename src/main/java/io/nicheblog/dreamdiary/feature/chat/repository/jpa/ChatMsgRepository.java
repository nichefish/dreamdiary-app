package io.nicheblog.dreamdiary.feature.chat.repository.jpa;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatMsgEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * ChatMsgRepository
 * <pre>
 *  채팅 메세지 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ChatMsgRepository
        extends BaseStreamRepository<ChatMsgEntity, Integer> {

    //
}

