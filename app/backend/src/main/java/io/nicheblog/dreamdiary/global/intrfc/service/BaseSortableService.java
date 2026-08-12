package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * BaseSortableService
 * <pre>
 *  (공통/상속) CRUD 공통 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseSortableService<SortOrderDto extends Identifiable<Key> & Sortable, Key extends Serializable, Entity extends BaseCrudEntity & Sortable>
        extends BaseEntityWritableService<Key, Entity> {

    /**
     * 정렬 순서 업데이트.
     *
     * @param sortOrders 키 + 정렬 순서로 이루어진 목록
     * @return {@link Boolean} -- 성공시 true 반환
     */
    @Transactional
    default ServiceResponse sortOrder(final List<SortOrderDto> sortOrders) throws Exception {
        if (CollectionUtils.isEmpty(sortOrders)) {
            return ServiceResponse.builder()
                    .rslt(true)
                    .build();
        }
        int i = 0;
        sortOrders.forEach(dto -> {
            try {
                final Entity e = this.getDtlEntity(dto.getKey());
                e.setSortOrder(dto.getSortOrder());
                this.updt(e);
            } catch (final Exception ex) {
                LogManager.getLogger(getClass()).warn("sortOrder 항목 처리 실패. key={}", dto.getKey(), ex);
                // 로그 기록, 예외 처리 등
                throw new RuntimeException(ex);
            }
        });

        // 변경 후처리
        this.postSortOrder(sortOrders);

        return ServiceResponse.builder()
                .rslt(true)
                .build();
    }

    /**
     * default: 정렬 순서 업데이트 후 해당 로직을 수행한다.
     *
     */
    default void postSortOrder(final List<SortOrderDto> sortOrders) throws Exception {
        // 변경 후처리:: 기본 공백, 필요시 각 함수에서 Override
    }
}
