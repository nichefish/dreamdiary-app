package io.nicheblog.dreamdiary.feature.attachable.prefix.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixScopeEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixRepository;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixScopeRepository;
import io.nicheblog.dreamdiary.feature.attachable.prefix.type.PrefixScopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Locale;

/**
 * 로그인 사용자의 개인 Prefix 관리와 사용자·게시판이 참조하는 Scope 공통 조회·선택 검증을 담당한다.
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class PrefixService {

    private final PrefixRepository repository;
    private final PrefixScopeRepository scopeRepository;

    /**
     * 로그인 사용자의 특정 content_type 말머리를 활성 상태와 무관하게 정렬 조회한다.
     * 해당 목록을 아직 만들지 않았으면(Scope 없음) 빈 목록을 반환한다.
     */
    @Transactional(readOnly = true)
    public List<PrefixDto> getMine(final String contentType) {
        final String username = AuthUtils.requireLoginUsername();
        return scopeRepository.findPersonalScope(username, contentType)
                .map(scope -> getAllByScope(scope.getId()))
                .orElseGet(List::of);
    }

    /**
     * 로그인 사용자의 특정 content_type 활성 말머리만 정렬 조회한다.
     * 해당 목록을 아직 만들지 않았으면(Scope 없음) 빈 목록을 반환한다.
     */
    @Transactional(readOnly = true)
    public List<PrefixDto> getActiveMine(final String contentType) {
        final String username = AuthUtils.requireLoginUsername();
        return scopeRepository.findPersonalScope(username, contentType)
                .map(scope -> getActiveByScope(scope.getId()))
                .orElseGet(List::of);
    }

    /**
     * 특정 content_type GLOBAL Scope의 Prefix를 비활성 상태와 무관하게 정렬 조회한다.
     * 첫 Prefix가 등록되지 않아 Scope가 없으면 빈 목록을 반환한다.
     */
    @Transactional(readOnly = true)
    public List<PrefixDto> getAllGlobal(final String contentType) {
        return findGlobalScope(contentType)
                .map(scope -> getAllByScope(scope.getId()))
                .orElseGet(List::of);
    }

    /**
     * 특정 content_type GLOBAL Scope에서 신규 선택 가능한 활성 Prefix만 정렬 조회한다.
     * 첫 Prefix가 등록되지 않아 Scope가 없으면 빈 목록을 반환한다.
     */
    @Transactional(readOnly = true)
    public List<PrefixDto> getActiveGlobal(final String contentType) {
        return findGlobalScope(contentType)
                .map(scope -> getActiveByScope(scope.getId()))
                .orElseGet(List::of);
    }

    /** 주어진 Scope의 말머리를 비활성 상태와 무관하게 정렬 조회한다. */
    @Transactional(readOnly = true)
    public List<PrefixDto> getAllByScope(final Integer scopeId) {
        requireScopeId(scopeId, "all-prefixes");
        return repository.findAllByScopeIdOrderBySortOrderAscIdAsc(scopeId).stream()
                .map(this::toDto)
                .toList();
    }

    /** 주어진 Scope에서 신규 선택 가능한 활성 말머리만 정렬 조회한다. */
    @Transactional(readOnly = true)
    public List<PrefixDto> getActiveByScope(final Integer scopeId) {
        requireScopeId(scopeId, "active-prefixes");
        return repository.findAllByScopeIdAndActiveYnOrderBySortOrderAscIdAsc(scopeId, "Y").stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 로그인 사용자의 특정 content_type 목록에 새 말머리를 등록한다.
     * 해당 (user, content_type) Scope가 없으면 이 시점에 lazy 생성한다.
     */
    @Transactional
    public PrefixDto create(final String contentType, final PrefixDto request) {
        final String username = AuthUtils.requireLoginUsername();
        final PrefixScopeEntity scope = findOrCreatePersonalScope(username, contentType);
        return createInScope(scope, request, "user:" + username + ":" + contentType);
    }

    /**
     * 특정 content_type GLOBAL 목록에 새 Prefix를 등록한다.
     * Scope가 없으면 이 시점에 {@code (GLOBAL, content_type)} Scope를 lazy 생성한다.
     */
    @Transactional
    public PrefixDto createGlobal(
            final String contentType,
            final PrefixDto request,
            final String scopeContext
    ) {
        final PrefixScopeEntity scope = findOrCreateGlobalScope(contentType, scopeContext);
        return createInScope(scope, request, scopeContext);
    }

    /** 주어진 Scope에 공통 불변식을 적용해 새 말머리를 등록한다. */
    @Transactional
    public PrefixDto createInScope(
            final PrefixScopeEntity scope,
            final PrefixDto request,
            final String scopeContext
    ) {
        final Integer scopeId = requireScope(scope, scopeContext).getId();
        final String name = normalizeRequiredName(request.getName());
        validateNameDuplicate(scopeId, name, null);
        final PrefixEntity saved = repository.save(PrefixEntity.builder()
                .scope(scope)
                .name(name)
                .color(normalizeColor(request.getColor()))
                .sortOrder(normalizeSortOrder(request.getSortOrder()))
                .activeYn("Y")
                .build());
        log.info("[Prefix] 등록. prefixId={}, scopeId={}, context={}", saved.getId(), scopeId, scopeContext);
        return toDto(saved);
    }

    /** 소유 말머리의 표시 속성을 수정한다. content_type 목록 소속을 검증한다. */
    @Transactional
    public PrefixDto update(final String contentType, final Integer prefixId, final PrefixDto request) {
        final String username = AuthUtils.requireLoginUsername();
        final PrefixScopeEntity scope = requirePersonalScope(username, contentType);
        return updateInScope(prefixId, scope.getId(), request, "user:" + username + ":" + contentType);
    }

    /** 특정 content_type GLOBAL Scope 소속 Prefix의 표시 속성을 수정한다. */
    @Transactional
    public PrefixDto updateGlobal(
            final String contentType,
            final Integer prefixId,
            final PrefixDto request,
            final String scopeContext
    ) {
        final PrefixScopeEntity scope = requireGlobalScope(contentType, scopeContext);
        return updateInScope(prefixId, scope.getId(), request, scopeContext);
    }

    /** 주어진 Scope 소속 말머리의 표시 속성을 공통 불변식 안에서 수정한다. */
    @Transactional
    public PrefixDto updateInScope(
            final Integer prefixId,
            final Integer scopeId,
            final PrefixDto request,
            final String scopeContext
    ) {
        requireScopeId(scopeId, scopeContext);
        final PrefixEntity prefix = requireInScope(prefixId, scopeId, scopeContext);
        final String name = normalizeRequiredName(request.getName());
        validateNameDuplicate(scopeId, name, prefixId);
        prefix.setName(name);
        prefix.setColor(normalizeColor(request.getColor()));
        prefix.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        final PrefixEntity saved = repository.save(prefix);
        log.info("[Prefix] 수정. prefixId={}, scopeId={}, context={}", prefixId, scopeId, scopeContext);
        return toDto(saved);
    }

    /** 기존 콘텐츠 참조를 보존한 채 활성 상태를 변경한다. content_type 목록 소속을 검증한다. */
    @Transactional
    public void setActive(final String contentType, final Integer prefixId, final boolean active) {
        final String username = AuthUtils.requireLoginUsername();
        final PrefixScopeEntity scope = requirePersonalScope(username, contentType);
        setActiveInScope(prefixId, scope.getId(), active, "user:" + username + ":" + contentType);
    }

    /** 특정 content_type GLOBAL Scope 소속 Prefix의 기존 참조를 보존한 채 활성 상태를 변경한다. */
    @Transactional
    public void setActiveGlobal(
            final String contentType,
            final Integer prefixId,
            final boolean active,
            final String scopeContext
    ) {
        final PrefixScopeEntity scope = requireGlobalScope(contentType, scopeContext);
        setActiveInScope(prefixId, scope.getId(), active, scopeContext);
    }

    /** 주어진 Scope 소속 말머리의 기존 참조를 보존한 채 활성 상태를 변경한다. */
    @Transactional
    public void setActiveInScope(
            final Integer prefixId,
            final Integer scopeId,
            final boolean active,
            final String scopeContext
    ) {
        requireScopeId(scopeId, scopeContext);
        final PrefixEntity prefix = requireInScope(prefixId, scopeId, scopeContext);
        prefix.setActiveYn(active ? "Y" : "N");
        repository.save(prefix);
        log.info("[Prefix] 활성 상태 변경. prefixId={}, activeYn={}, scopeId={}, context={}",
                prefixId, prefix.getActiveYn(), scopeId, scopeContext);
    }

    /** 저장 요청의 말머리가 로그인 사용자의 해당 content_type 목록 소속이며 선택 가능한지 검증한다. */
    @Transactional(readOnly = true)
    public PrefixEntity requireSelectable(final String contentType, final Integer prefixId) {
        return requireSelectable(contentType, prefixId, null);
    }

    /**
     * 기존 콘텐츠가 같은 비활성 Prefix를 유지하는 경우만 허용하고 신규 비활성 선택은 거부한다.
     */
    @Transactional(readOnly = true)
    public PrefixEntity requireSelectable(final String contentType, final Integer prefixId, final Integer currentPrefixId) {
        if (prefixId == null) return null;
        final String username = AuthUtils.requireLoginUsername();
        final PrefixScopeEntity scope = requirePersonalScope(username, contentType);
        return requireSelectableInScope(prefixId, scope.getId(), currentPrefixId, "user:" + username + ":" + contentType);
    }

    /**
     * 저장 요청의 Prefix가 특정 content_type GLOBAL Scope 소속이며 선택 가능한지 검증한다.
     * Prefix 선택이 없으면 Scope가 아직 생성되지 않은 정상 상태도 허용한다.
     */
    @Transactional(readOnly = true)
    public PrefixEntity requireSelectableGlobal(
            final String contentType,
            final Integer prefixId,
            final Integer currentPrefixId,
            final String scopeContext
    ) {
        if (prefixId == null) return null;
        final PrefixScopeEntity scope = requireGlobalScope(contentType, scopeContext);
        return requireSelectableInScope(prefixId, scope.getId(), currentPrefixId, scopeContext);
    }

    /**
     * 저장 요청의 말머리가 주어진 Scope에 속하며 선택 가능한지 검증한다.
     * 기존 콘텐츠가 같은 비활성 Prefix를 유지하는 경우만 허용하고 신규 비활성 선택은 거부한다.
     */
    @Transactional(readOnly = true)
    public PrefixEntity requireSelectableInScope(
            final Integer prefixId,
            final Integer scopeId,
            final Integer currentPrefixId,
            final String scopeContext
    ) {
        if (prefixId == null) return null;
        if (scopeId == null) {
            log.error("[Prefix] 선택 검증 Scope 누락. prefixId={}, context={}", prefixId, scopeContext);
            throw new IllegalStateException("Prefix Scope is missing.");
        }
        final PrefixEntity prefix = requireInScope(prefixId, scopeId, scopeContext);
        if (!"Y".equals(prefix.getActiveYn()) && !prefixId.equals(currentPrefixId)) {
            log.warn("[Prefix] 비활성 말머리 신규 선택 거부. prefixId={}, scopeId={}, context={}",
                    prefixId, scopeId, scopeContext);
            throw new IllegalStateException("Inactive Prefix cannot be selected.");
        }
        return prefix;
    }

    /**
     * 로그인 사용자의 (content_type) 개인 Scope를 반환한다. 없으면 오류.
     * <p>
     * 수정·활성변경·선택검증처럼 이미 존재하는 목록을 전제로 하는 경로에서 사용한다.
     * 조회(getMine 등)는 빈 목록을, 등록(create)은 lazy 생성을 쓰므로 이 메서드를 쓰지 않는다.
     * </p>
     */
    private PrefixScopeEntity requirePersonalScope(final String username, final String contentType) {
        return scopeRepository.findPersonalScope(username, contentType)
                .orElseThrow(() -> {
                    log.error("[Prefix] 사용자 Prefix Scope 누락. username={}, contentType={}", username, contentType);
                    return new IllegalStateException("Personal Prefix Scope is missing.");
                });
    }

    /**
     * 로그인 사용자의 (content_type) 개인 Scope를 조회하고, 없으면 lazy 생성한다.
     * <p>
     * 사전(가입 시점) 프로비저닝 없이, 사용자가 해당 content_type의 첫 말머리를 등록하는
     * 시점에 {@code (PERSONAL, user_id, content_type)} Scope 행을 만든다.
     * </p>
     */
    private PrefixScopeEntity findOrCreatePersonalScope(final String username, final String contentType) {
        return scopeRepository.findPersonalScope(username, contentType)
                .orElseGet(() -> {
                    final Integer userId = scopeRepository.findUserIdByUsername(username)
                            .orElseThrow(() -> {
                                log.error("[Prefix] 사용자 조회 실패. username={}", username);
                                return new IllegalStateException("User not found.");
                            });
                    final PrefixScopeEntity created = scopeRepository.save(PrefixScopeEntity.builder()
                            .scopeType(PrefixScopeType.PERSONAL)
                            .userId(userId)
                            .contentType(contentType)
                            .build());
                    log.info("[Prefix] 개인 Scope lazy 생성. username={}, contentType={}, scopeId={}",
                            username, contentType, created.getId());
                    return created;
                });
    }

    /** 특정 content_type의 GLOBAL Scope를 조회한다. */
    private java.util.Optional<PrefixScopeEntity> findGlobalScope(final String contentType) {
        return scopeRepository.findByScopeTypeAndContentType(PrefixScopeType.GLOBAL, contentType);
    }

    /**
     * 특정 content_type의 GLOBAL Scope를 반환한다. 첫 Prefix 등록 전의 조회는 빈 목록이지만
     * 수정·활성변경·선택검증은 기존 Scope를 전제로 하므로 누락 시 오류로 드러낸다.
     */
    private PrefixScopeEntity requireGlobalScope(final String contentType, final String scopeContext) {
        return findGlobalScope(contentType)
                .orElseThrow(() -> {
                    log.error("[Prefix] GLOBAL Prefix Scope 누락. contentType={}, context={}", contentType, scopeContext);
                    return new IllegalStateException("Global Prefix Scope is missing.");
                });
    }

    /** 첫 Prefix 등록 시 특정 content_type GLOBAL Scope를 lazy 생성한다. */
    private PrefixScopeEntity findOrCreateGlobalScope(final String contentType, final String scopeContext) {
        return findGlobalScope(contentType)
                .orElseGet(() -> {
                    final PrefixScopeEntity created = scopeRepository.save(PrefixScopeEntity.builder()
                            .scopeType(PrefixScopeType.GLOBAL)
                            .userId(null)
                            .contentType(contentType)
                            .build());
                    log.info("[Prefix] GLOBAL Scope lazy 생성. contentType={}, scopeId={}, context={}",
                            contentType, created.getId(), scopeContext);
                    return created;
                });
    }

    /** 공통 쓰기 요청의 Scope 엔티티와 영속 식별자를 검증한다. */
    private PrefixScopeEntity requireScope(final PrefixScopeEntity scope, final String scopeContext) {
        if (scope == null || scope.getId() == null) {
            log.error("[Prefix] 쓰기 Scope 누락. context={}", scopeContext);
            throw new IllegalStateException("Prefix Scope is missing.");
        }
        return scope;
    }

    /** 공통 조회·쓰기 요청의 Scope 식별자를 검증한다. */
    private void requireScopeId(final Integer scopeId, final String scopeContext) {
        if (scopeId == null) {
            log.error("[Prefix] Scope 식별자 누락. context={}", scopeContext);
            throw new IllegalStateException("Prefix Scope is missing.");
        }
    }

    /** 요청 Prefix가 주어진 Scope에 속하는지 검증한다. */
    private PrefixEntity requireInScope(final Integer prefixId, final Integer scopeId, final String scopeContext) {
        final PrefixEntity prefix = repository.findById(prefixId)
                .orElseThrow(() -> new EntityNotFoundException("Prefix not found."));
        final Integer actualScopeId = prefix.getScope() != null ? prefix.getScope().getId() : null;
        if (!scopeId.equals(actualScopeId)) {
            log.warn("[Prefix] Scope 접근 거부. prefixId={}, requestedScopeId={}, actualScopeId={}, context={}",
                    prefixId, scopeId, actualScopeId, scopeContext);
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
        return prefix;
    }

    private void validateNameDuplicate(final Integer scopeId, final String name, final Integer excludedId) {
        final boolean duplicate = excludedId == null
                ? repository.existsByScopeIdAndNameIgnoreCase(scopeId, name)
                : repository.existsByScopeIdAndNameIgnoreCaseAndIdNot(scopeId, name, excludedId);
        if (duplicate) throw new IllegalArgumentException("Prefix name already exists.");
    }

    private String normalizeRequiredName(final String rawName) {
        final String name = StringUtils.trimToEmpty(rawName);
        if (name.isEmpty()) throw new IllegalArgumentException("Prefix name is required.");
        if (name.length() > 100) throw new IllegalArgumentException("Prefix name is too long.");
        return name;
    }

    private String normalizeColor(final String rawColor) {
        final String color = StringUtils.trimToNull(rawColor);
        if (color == null) return null;
        if (!color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Prefix color must be #RRGGBB.");
        }
        return color.toUpperCase(Locale.ROOT);
    }

    private Integer normalizeSortOrder(final Integer sortOrder) {
        return sortOrder == null ? 0 : Math.max(sortOrder, 0);
    }

    private PrefixDto toDto(final PrefixEntity entity) {
        return PrefixDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .color(entity.getColor())
                .sortOrder(entity.getSortOrder())
                .activeYn(entity.getActiveYn())
                .build();
    }
}
