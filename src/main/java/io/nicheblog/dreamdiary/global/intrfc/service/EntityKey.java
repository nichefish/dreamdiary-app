package io.nicheblog.dreamdiary.global.intrfc.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 캐시 처리용 엔티티 키를 정의하는 열거형 클래스입니다.
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum EntityKey {
    REGIST_ENTITY("registEntity"),
    MODIFY_ENTITY("modifyEntity"),
    UPDATED_ENTITY("updatedEntity"),
    ENTITY_LIST("entityList"),
    DELETE_ENTITY("deleteEntity");

    @Getter
    private final String key;
}
