package io.nicheblog.dreamdiary.domain.jrnl.entry.service;

import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.mapstruct.JrnlEntryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntrySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.entry.repository.jpa.JrnlEntryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.entry.repository.mybatis.JrnlEntryMapper;
import io.nicheblog.dreamdiary.domain.jrnl.entry.spec.JrnlEntrySpec;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.model.JrnlIntrptDto;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagProcEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * JrnlEntryService
 * <pre>
 *  저널 항목 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlEntryService")
@RequiredArgsConstructor
@Log4j2
public class JrnlEntryService
        implements BaseClsfService<JrnlEntryDto, JrnlEntryDto, Integer, JrnlEntryEntity> {

    @Getter
    private final JrnlEntryRepository repository;
    @Getter
    private final JrnlEntrySpec spec;
    @Getter
    private final JrnlEntryMapstruct mapstruct = JrnlEntryMapstruct.INSTANCE;

    public JrnlEntryMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlEntryMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JrnlEntryMapper jrnlEntryMapper;
    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private JrnlEntryService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 특정 태그의 관련 항목 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     */
    @Cacheable(value="myJrnlEntryTagDtl", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getTagNo()")
    public List<JrnlEntryDto> jrnlEntryTagDtl(final JrnlEntrySearchParam searchParam) throws Exception {
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JrnlEntryDto registDto) throws Exception {
        // 인덱스(정렬순서) 처리
        final Integer lastIndex = repository.findLastIndexByJrnlDay(registDto.getJrnlDayNo()).orElse(0);
        registDto.setIdx(lastIndex + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlEntryDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_ENTRY));
    }
    
    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JrnlEntryDto modifyDto, final JrnlEntryEntity modifyEntity) throws Exception {
        boolean isIdxChanged = !Objects.equals(modifyDto.getIdx(), modifyEntity.getIdx());
        modifyDto.setIsIdxChanged(isIdxChanged);
    }
    
    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlEntryDto postDto, final JrnlEntryDto updatedDto) throws Exception {
        // 인덱스 재조정
        if (updatedDto.getIsIdxChanged()) this.getSelf().reorderIdx(updatedDto);

        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_ENTRY));
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JrnlEntryDto deletedDto) throws Exception {
        // 인덱스 재조정
        this.getSelf().reorderIdx(deletedDto);

        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, deletedDto.getClsfKey(), deletedDto.getYy(), deletedDto.getMnth()));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_ENTRY));
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlIntrptDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JrnlEntryDto getDeletedDtlDto(final Integer key) throws Exception {
        return jrnlEntryMapper.getDeletedByPostNo(key);
    }
    
    /**
     * 해당 그룹 전체를 idx = 1부터 다시 정렬한다.
     *
     * @param jrnlDayNo 정렬을 수행할 상위 키
     */
    @Transactional
    public void normalize(final Integer jrnlDayNo) {
        final List<JrnlEntryDto> list = jrnlEntryMapper.findAllForReorder(jrnlDayNo);
        if (CollectionUtils.isEmpty(list) || list.size() == 1) return;

        int idx = 1;
        for (final JrnlEntryDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictCache("myJrnlEntryDtlDto", e.getPostNo());
        }

        jrnlEntryMapper.batchUpdateIdx(list);
    }
    
    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param jrnlDayNo 정렬을 수행할 상위 키
     * @param postNo 게시물 PK
     * @param targetIdx 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer jrnlDayNo, final Integer postNo, Integer targetIdx) throws Exception {
        final List<JrnlEntryDto> list = jrnlEntryMapper.findAllForReorder(jrnlDayNo);

        // target 조회
        final JrnlEntryEntity targetEntity = findDtlEntity(postNo);
        final JrnlEntryDto target = mapstruct.toDto(targetEntity);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getPostNo(), postNo));

        // entryNo 변경
        target.setJrnlDayNo(jrnlDayNo);

        // targetIdx 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetIdx == null ? maxIdx : targetIdx, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // idx 재정렬
        int idx = 1;
        for (final JrnlEntryDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictCache("myJrnlEntryDtlDto", e.getPostNo());
        }

        jrnlEntryMapper.batchUpdateIdx(list);
    }

    /**
     * 인덱스 변경시 관련 인덱스 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderIdx(final JrnlEntryDto updatedDto) throws Exception {
        // 1단계: 현재 entry 그룹 정리 (기존 idx 값을 normalization하여 안정화)
        normalize(updatedDto.getJrnlDayNo());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJrnlDayNo(), updatedDto.getPostNo(), updatedDto.getIdx());
    }
}