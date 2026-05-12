package io.nicheblog.dreamdiary.global.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ReflectionUtils
 * <pre>
 *  리플렉션 관련 유틸리티 클래스
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public class ReflectionUtils {

    /**
     * 필드에 값 세팅 
     */    
    public static void setField(final Object obj, final String fieldNm, final Object value) {
        try {
            final Field field = obj.getClass().getDeclaredField(fieldNm);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field value", e);
        }
    }

    /**
     * 필드 값 조회 
     */
    public static Object getField(final Object obj, final String fieldNm) {
        try {
            final Field field = obj.getClass().getDeclaredField(fieldNm);
            field.setAccessible(true);
            return field.get(obj);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value", e);
        }
    }

    /**
     * 메소드 실행 (+및 결과 조회)
     */
    public static Object invokeMethod(final Object obj, final String methodNm, final Object... args) {
        try {
            final Class<?>[] argClasses = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argClasses[i] = args[i].getClass();
            }
            final Method method = obj.getClass().getDeclaredMethod(methodNm, argClasses);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method", e);
        }
    }
}
