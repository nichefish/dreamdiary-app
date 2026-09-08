package io.nicheblog.dreamdiary.feature.journal.todo.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.todo.entity.JournalTodoEntity;
import io.nicheblog.dreamdiary.feature.journal.todo.mapstruct.JournalTodoMapstruct;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoDto;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoSearchParam;
import io.nicheblog.dreamdiary.feature.journal.todo.repository.jpa.JournalTodoRepository;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleViewHelper;
import io.nicheblog.dreamdiary.feature.journal.todo.spec.JournalTodoSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JournalTodoService
 * <pre>
 *  저널 일기 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalTodoService
        implements BaseAttachableService<JournalTodoDto, JournalTodoDto, Integer, JournalTodoEntity> {

    @Getter
    private final JournalTodoRepository repository;
    @Getter
    private final JournalTodoSpec spec;
    @Getter
    private final JournalTodoMapstruct mapstruct = JournalTodoMapstruct.INSTANCE;

    public JournalTodoMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalTodoMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalCacheEvictWorker journalCacheEvictWorker;
    private final LifecycleService lifecycleService;

    private final ApplicationContext context;
    private JournalTodoService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 활성 할일 목록 조회 (dto level).
     * <p>
     * 월(yy/mnth)에 종속되지 않고 사용자의 활성 집합(OPEN·PENDING)을 발생지 무관하게 투영한다.
     * RESOLVED 는 활성 투영에서 제외한다. yy/mnth 는 발생지(provenance)로 보존되며 조회 축이 아니라
     * 여기서 무시한다. 소규모 개인 데이터라 리스트 캐시 없이 매 조회 신선하게 lifecycle 을 부착한다.
     * (RESOLVED 제외는 인메모리 필터다. 카디널리티가 커지면 lifecycle 조인으로 쿼리 단계로 내린다.)
     * </p>
     *
     * @param username 사용자 계정명
     * @param searchParam 검색 조건이 담긴 파라미터 객체 (yy/mnth 는 무시)
     * @return {@link List} -- 활성 할일 목록
     */
    public List<JournalTodoDto> getActiveListDtoByUser(final String username, final JournalTodoSearchParam searchParam) throws Exception {
        return this.getListDtoWithLifecycleByUser(username, searchParam).stream()
                .filter(dto -> dto.getLifecycle() == null || !dto.getLifecycle().is(LifecycleKey.RESOLVED))
                .collect(Collectors.toList());
    }

    /**
     * 사용자 전 범위 할일 목록 조회 (dto level).
     * <p>
     * 발생지(yy/mnth) 무관하게 사용자의 모든 할일을 lifecycle 부착해 반환한다. 활성 집합과 달리
     * RESOLVED 를 제외하지 않는다 -- 전수관리(keep) 화면이 완료·미해결을 함께 투영하기 위함이다.
     * yy/mnth 는 발생지(provenance)로 보존되며 조회 축이 아니라 여기서 무시한다. 소규모 개인
     * 데이터라 리스트 캐시 없이 매 조회 신선하게 lifecycle 을 부착한다.
     * </p>
     *
     * @param username 사용자 계정명
     * @param searchParam 검색 조건이 담긴 파라미터 객체 (yy/mnth 는 무시)
     * @return {@link List} -- 사용자 전 범위 할일 목록 (lifecycle 부착)
     */
    public List<JournalTodoDto> getListDtoWithLifecycleByUser(final String username, final JournalTodoSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        searchParam.setYy(null);
        searchParam.setMnth(null);

        final List<JournalTodoDto> listDto = this.getSelf().getListDto(searchParam);
        this.applyTodoLifecycles(listDto);
        return listDto;
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalTodoDto registDto) throws Exception {
        // 정렬 순서 처리 :: 월 무관 사용자 전역 순번 (활성 집합 cross-month 우선순위)
        final String username = AuthUtils.requireLoginUsername();
        final Integer lastSortOrder = repository.findLastIndexByCreatedBy(username).orElse(0);
        registDto.setSortOrder(lastSortOrder + 1);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체 (dto)
     * @param modifyEntity 수정할 객체 (entity)
     */
    @Override
    public void preModify(final JournalTodoDto modifyDto, final JournalTodoEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalTodoDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_TODO);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalTodoDto postDto, final JournalTodoDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_TODO);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JournalTodoDto} -- 조회된 객체
     */
    @Cacheable(value="journalTodoDetailDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalTodoDto getDetailDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalTodoEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalTodoDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) throw new NotAuthorizedException("common.result.access-not-authorized");
        return retrieved;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalTodoDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JournalTodoDto deletedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_TODO);
    }

    /**
     * 할일 DTO 목록에 라이프사이클을 부착한다.
     * <p>
     * 부착 테이블에 행이 없으면 {@code OPEN}. 스레드·엔트리 enrich 와 동일 계약이다.
     * </p>
     *
     * @param dtoList 대상 할일 DTO 목록
     */
    private void applyTodoLifecycles(final List<JournalTodoDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) return;
        final List<Integer> todoIds = dtoList.stream()
                .map(JournalTodoDto::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (todoIds.isEmpty()) return;
        JournalLifecycleViewHelper.applyTodoLifecycle(
                dtoList,
                lifecycleService.getLifecycleMap(ContentType.JOURNAL_TODO, todoIds)
        );
    }
}

