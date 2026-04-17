package io.nicheblog.dreamdiary.feature.chat.repository.jpa;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatMessageEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * ChatMessageRepository
 * <pre>
 *  채팅 메세지 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ChatMessageRepository
        extends BaseStreamRepository<ChatMessageEntity, Integer> {

    //
}
