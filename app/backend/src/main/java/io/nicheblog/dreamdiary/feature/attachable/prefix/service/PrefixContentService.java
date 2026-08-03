package io.nicheblog.dreamdiary.feature.attachable.prefix.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;

/**
 * 콘텐츠의 말머리 선택(0..1)을 prefix_content 연결로 반영하는 서비스.
 * <p>
 * 콘텐츠당 최대 1건이라 meta의 이벤트·캐시 기계 없이 단순 upsert/삭제만 수행한다.
 * 선택 가능 여부(소유·활성)는 PERSONAL/GLOBAL Scope별 {@link PrefixService} 검증 경로가
 * 최종 확정하며, 연결 생성·교체·해제 로직은 이 서비스의 단일 경로를 사용한다.
 * </p>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class PrefixContentService {

    private final PrefixContentRepository repository;
    private final PrefixService prefixService;

    /**
     * 콘텐츠의 말머리 선택을 반영한다.
     * <p>
     * 기존 연결의 말머리를 현재 선택으로 간주해 신규/유지 여부를 검증한 뒤,
     * {@code prefixId}가 null이면 연결을 제거하고, 있으면 연결을 생성하거나 교체한다.
     * </p>
     *
     * @param key 콘텐츠 attachable 키(글 번호 + 컨텐츠 타입)
     * @param contentType 목록 적용 대상 콘텐츠 타입(선택 검증용)
     * @param prefixId 선택한 말머리 ID(0..1, null이면 선택 해제)
     * @return 반영된 말머리 엔티티(표시용). 선택 해제 시 null.
     */
    @Transactional
    public PrefixEntity applySelection(final BaseAttachableKey key, final String contentType, final Integer prefixId) {
        return applySelection(
                key,
                prefixId,
                (requestedPrefixId, currentPrefixId) ->
                        prefixService.requireSelectable(contentType, requestedPrefixId, currentPrefixId)
        );
    }

    /**
     * GLOBAL Scope를 사용하는 콘텐츠의 말머리 선택을 반영한다.
     * 게시글은 {@code ref_content_type=boardKey}와 {@code GLOBAL + boardKey} Scope를 같은
     * contentType으로 사용하며, 기존 비활성 선택 유지 계약도 PERSONAL 경로와 동일하게 적용한다.
     *
     * @param key 콘텐츠 attachable 키
     * @param contentType GLOBAL Scope의 논리 콘텐츠 타입
     * @param prefixId 선택한 말머리 ID
     * @param scopeContext 오류·감사 로그 문맥
     * @return 반영된 말머리 엔티티. 선택 해제 시 null.
     */
    @Transactional
    public PrefixEntity applyGlobalSelection(
            final BaseAttachableKey key,
            final String contentType,
            final Integer prefixId,
            final String scopeContext
    ) {
        return applySelection(
                key,
                prefixId,
                (requestedPrefixId, currentPrefixId) ->
                        prefixService.requireSelectableGlobal(
                                contentType, requestedPrefixId, currentPrefixId, scopeContext
                        )
        );
    }

    /**
     * Scope 유형별 선택 검증 결과를 받아 prefix_content 연결을 생성·교체·해제한다.
     * 검증 전략만 분리하고 영속 변경 순서는 PERSONAL/GLOBAL 모두 동일하게 유지한다.
     */
    private PrefixEntity applySelection(
            final BaseAttachableKey key,
            final Integer prefixId,
            final BiFunction<Integer, Integer, PrefixEntity> selectionValidator
    ) {
        final PrefixContentEntity existing = repository
                .findByRefIdAndRefContentType(key.getId(), key.getContentType())
                .orElse(null);
        final Integer currentPrefixId = existing == null ? null : existing.getPrefixId();

        // 소유·활성 검증. 기존 선택(currentPrefixId)과 같으면 비활성 유지도 허용된다.
        final PrefixEntity prefix = selectionValidator.apply(prefixId, currentPrefixId);

        if (prefixId == null) {
            if (existing != null) {
                repository.delete(existing);
                log.info("[PrefixContent] 선택 해제. refId={}, contentType={}", key.getId(), key.getContentType());
            }
            return null;
        }
        if (existing == null) {
            repository.save(new PrefixContentEntity(prefixId, key));
            log.info("[PrefixContent] 선택 생성. refId={}, contentType={}, prefixId={}",
                    key.getId(), key.getContentType(), prefixId);
        } else if (!prefixId.equals(currentPrefixId)) {
            existing.setPrefixId(prefixId);
            repository.save(existing);
            log.info("[PrefixContent] 선택 교체. refId={}, contentType={}, prefixId={}",
                    key.getId(), key.getContentType(), prefixId);
        }
        return prefix;
    }
}
