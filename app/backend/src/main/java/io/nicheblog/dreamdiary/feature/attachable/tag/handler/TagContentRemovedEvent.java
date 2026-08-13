package io.nicheblog.dreamdiary.feature.attachable.tag.handler;

import lombok.Getter;

import java.util.Collection;

/**
 * 태그-컨텐츠 연결이 제거되었을 때 발행되는 이벤트.
 * 리스너가 비동기로 고아 마스터 태그를 정리한다.
 */
@Getter
public class TagContentRemovedEvent {

    private final Collection<Integer> removedTagIds;

    public TagContentRemovedEvent(final Collection<Integer> removedTagIds) {
        this.removedTagIds = removedTagIds;
    }

}
