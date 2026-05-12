package io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import lombok.experimental.UtilityClass;
import org.mapstruct.MappingTarget;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * MapstructHelper
 * <pre>
 *  MapStruct 공통 후처리 helper.
 * </pre>
 * 
 * @author nichefish 
 */
@UtilityClass
public final class MapstructHelper {

    /** Auth 관련 MapStruct Helper의 FQCN */
    private static final String AUTH_HELPER_FQCN = "io.nicheblog.dreamdiary.auth.intrfc.mapstruct.helper.AuthMapstructHelper";
    /** helper resolve 여부 (double-checked locking용) */
    private static volatile boolean authHelperResolved = false;
    /** 캐싱된 mapAuditFields 메서드 */
    private static Method authMapMethod;

    /**
     * Map Base-inheritted Fields (entity -> dto)
     *
     * @param entity 매핑할 Entity
     * @param dto 매핑 대상 Dto
     */
    public static <Entity extends BaseCrudEntity, Dto extends BaseCrudDto> void mapBaseFields(
            final Entity entity,
            final @MappingTarget Dto dto
    ) throws Exception {
        mapAuthFields(entity, dto);
        // ATTACHABLE fields are handled by BaseAttachableMapstruct.
    }

    /**
     * AuthMapstructHelper의 mapAuditFields 메서드를 reflection으로 조회한다.
     *
     * <pre>
     * 동작 방식:
     * - 최초 호출 시 Class.forName + getMethod 수행
     * - 이후 Method를 캐싱하여 재사용
     * - 클래스 또는 메서드가 존재하지 않을 경우 null 반환
     * </pre>
     *
     * @return mapAuditFields 메서드 또는 null (helper 미존재 시)
     */
    private static Method resolveAuthMapMethod() {
        if (authHelperResolved) return authMapMethod;

        synchronized (MapstructHelper.class) {
            if (authHelperResolved) return authMapMethod;

            try {
                final Class<?> helperClass = Class.forName(AUTH_HELPER_FQCN);
                authMapMethod = helperClass.getMethod("mapAuditFields", Object.class, Object.class);
            } catch (final ReflectiveOperationException ignored) {
                authMapMethod = null;
            } finally {
                authHelperResolved = true;
            }
        }
        return authMapMethod;
    }

    /**
     * Auth 관련 audit 필드를 매핑한다. (AuthMapstructHelper가 존재할 경우에만 실행)
     *
     * @param entity source entity
     * @param dto target dto
     * @throws Exception helper 내부에서 발생한 예외 전달
     */
    private static <Entity extends BaseCrudEntity, Dto extends BaseCrudDto> void mapAuthFields(
            final Entity entity,
            final @MappingTarget Dto dto
    ) throws Exception {
        final Method method = resolveAuthMapMethod();
        if (method == null) return;

        try {
            method.invoke(null, entity, dto);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getTargetException();
            if (cause instanceof Exception exception) throw exception;
            throw new RuntimeException(cause);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to invoke auth mapstruct helper.", e);
        }
    }
}
