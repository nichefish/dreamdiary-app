package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.intrfc.entity.BasePostEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.model.BasePostDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BasePostService
 * <pre>
 *  (공통/상속) 일반게시물 CRUD 공통 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BasePostService<Dto extends BasePostDto & Identifiable<Key>, ListDto extends BasePostDto, Key extends Serializable, Entity extends BasePostEntity, Repository extends BaseStreamRepository<Entity, Key>, Spec extends BaseSpec<Entity>, Mapstruct extends BaseClsfMapstruct<Dto, ListDto, Entity>>
        extends BaseClsfService<Dto, ListDto, Key, Entity, Repository, Spec, Mapstruct> {

    /**
     * default: 상단 고정 항목 목록을 조회한다.
     * TODO: 조회조건 좀 더 세분화?
     *
     * @return List<ListDto> - 상단 고정 항목 목록
     * @throws Exception - 조회 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default List<ListDto> getFxdList() throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("fxdYn", "Y");
        }};
        return this.getListDto(searchParamMap);
    }

    /**
     * default: 상세 페이지 조회 후처리 (dto level)
     *
     * @param retrievedDto - 조회된 Dto 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    default void postViewDtlPage(final Dto retrievedDto) throws Exception {
        // 상세 페이지 조회 후처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 상세 페이지 조회
     *
     * @param key 조회수를 증가시킬 항목의 키
     * @return Dto -- 조회된 객체
     * @throws Exception 조회수 증가 중 발생할 수 있는 예외
     */
    @Transactional
    default Dto viewDtlPage(final Key key) throws Exception {
        final Dto retrievedDto = this.getDtlDto(key);

        // 조회수 증가
        this.hitCntUp(key);

        // optional: 상세 페이지 조회 후처리 (dto)
        this.postViewDtlPage(retrievedDto);

        return retrievedDto;
    }

    /**
     * default: 항목 조회수를 1 증가시킨다.
     *
     * @param key 조회수를 증가시킬 항목의 키
     * @throws Exception 조회수 증가 중 발생할 수 있는 예외
     */
    @Transactional
    default void hitCntUp(final Key key) throws Exception {
        final Entity e = this.getDtlEntity(key);
        final String lgnUserId = AuthUtils.getLgnUserId();
        if (StringUtils.isEmpty(lgnUserId)) return;
        if (lgnUserId.equals(e.getRegstrId())) return;      // 본인이 쓴 글은 조회수 증가하지 않음

        final Integer currentHitCnt = e.getHitCnt();
        e.setHitCnt(currentHitCnt + 1);
        this.updt(e);
    }
}
