package io.nicheblog.dreamdiary.global.type;

import io.nicheblog.dreamdiary.global.util.MessageUtils;

/**
 * LocalizedEnum
 * <pre>
 *  다국어 label 을 제공하는 Enum 공통 인터페이스.
 *  구현 Enum 은 별도 메서드 없이 {@code implements LocalizedEnum} 만 선언하면 된다.
 *
 *  메시지 키 컨벤션: {@code enum.{class-kebab}.{name-lower}}
 *  예) {@code LifecycleKey.OPEN} → {@code enum.lifecycle-key.open}
 *      {@code Auth.USER}         → {@code enum.auth.user}
 *
 *  실제 메시지는 {@code messages_ko.properties} / {@code messages_en.properties} 에 정의한다.
 * </pre>
 *
 * @author nichefish
 */
public interface LocalizedEnum {

    /**
     * 현재 locale 에 맞는 label 을 반환한다.
     * 메시지 키를 찾지 못하면 키 문자열을 그대로 반환한다.
     *
     * @return {@link String} -- 현재 locale 에 맞는 label
     */
    default String getLabel() {
        final String className = this.getClass().getSimpleName()
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .toLowerCase();
        final String enumName = ((Enum<?>) this).name().toLowerCase();
        return MessageUtils.getMessage("enum." + className + "." + enumName);
    }
}
