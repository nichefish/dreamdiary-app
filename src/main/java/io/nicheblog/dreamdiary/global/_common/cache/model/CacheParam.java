package io.nicheblog.dreamdiary.global._common.cache.model;

import lombok.Getter;
import lombok.Setter;

/**
 * CacheParam
 * <pre>
 *  캐시 관련 파라미터
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
public class CacheParam {

    /** 삭제할 캐시의 이름 */
    private String cacheName;

    /** 삭제할 캐시 항목의 키 (전체 삭제 시 "-" 입력) */
    private String cacheKey;
}
