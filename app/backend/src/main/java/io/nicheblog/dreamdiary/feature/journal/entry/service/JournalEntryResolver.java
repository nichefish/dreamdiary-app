package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryPolicyResolver;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * JournalEntryResolver
 * <pre>
 *  ref key(id/contentType) 기반 저널 엔트리 조회·제목·작성자·제목맵 해석을 담당한다.
 *  BaseAttachableService 라이프사이클과 분리해, related-content 등 외부 참조자가 순환 없이 재사용한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class JournalEntryResolver {

    private final JournalEntryRepository repository;
    private final JournalEntryPolicyResolver policyResolver;

    /**
     * ref(id + contentType) 기반으로 엔트리를 안전 조회한다.
     *
     * @param id 엔트리 ID
     * @param contentType 콘텐츠 타입 문자열
     * @return 조회 결과
     */
    public Optional<JournalEntryEntity> findByRef(final Integer id, final String contentType) {
        if (id == null || StringUtils.isBlank(contentType) || !policyResolver.isEntryType(contentType)) {
            return Optional.empty();
        }
        return repository.findByIdAndContentType(id, contentType);
    }

    /**
     * enum 타입 콘텐츠를 문자열 키로 변환해 ref 조회한다.
     *
     * @param id 엔트리 ID
     * @param contentType 콘텐츠 타입 enum
     * @return 조회 결과
     */
    public Optional<JournalEntryEntity> findByRef(final Integer id, final ContentType contentType) {
        return this.findByRef(id, contentType != null ? contentType.key : null);
    }

    /**
     * 복합 ref key를 사용해 엔트리를 조회한다.
     *
     * @param refKey 복합 키
     * @return 조회 결과
     */
    public Optional<JournalEntryEntity> findByRef(final BaseAttachableKey refKey) {
        if (refKey == null) return Optional.empty();
        return this.findByRef(refKey.getId(), refKey.getContentType());
    }

    /**
     * ref key에 해당하는 엔트리 제목을 반환한다.
     *
     * @param refKey 복합 키
     * @return 제목
     */
    public String resolveTitle(final BaseAttachableKey refKey) {
        return this.findByRef(refKey)
                .map(JournalEntryEntity::getTitle)
                .orElse(null);
    }

    /**
     * ref key에 해당하는 작성자 아이디를 반환한다.
     *
     * @param refKey 복합 키
     * @return 작성자 아이디
     */
    public String resolveCreatedBy(final BaseAttachableKey refKey) {
        return this.findByRef(refKey)
                .map(JournalEntryEntity::getCreatedBy)
                .orElse(null);
    }

    /**
     * ref key 목록을 제목 맵(contentType:id -> title)으로 변환한다.
     *
     * @param refKeyList 복합 키 목록
     * @return 제목 맵
     */
    public Map<String, String> resolveTitleMap(final Collection<BaseAttachableKey> refKeyList) {
        final Map<String, String> titleMap = new LinkedHashMap<>();
        if (refKeyList == null || refKeyList.isEmpty()) return titleMap;

        final Set<Integer> idSet = new LinkedHashSet<>();
        final Set<String> contentTypeSet = new LinkedHashSet<>();
        for (final BaseAttachableKey refKey : refKeyList) {
            if (refKey == null || refKey.getId() == null || !this.isJournalEntryType(refKey.getContentType())) continue;
            idSet.add(refKey.getId());
            contentTypeSet.add(refKey.getContentType());
        }
        if (idSet.isEmpty() || contentTypeSet.isEmpty()) return titleMap;

        repository.findAllByIdInAndContentTypeIn(idSet, contentTypeSet).forEach(entity ->
                titleMap.put(this.toKey(entity.getContentType(), entity.getId()), entity.getTitle())
        );
        return titleMap;
    }

    /**
     * 문자열 콘텐츠 타입이 엔트리 타입인지 검사한다.
     *
     * @param contentType 콘텐츠 타입 문자열
     * @return 엔트리 타입 여부
     */
    public boolean isJournalEntryType(final String contentType) {
        return policyResolver.isEntryType(contentType);
    }

    /**
     * 캐시 키/맵 키 공통 포맷을 생성한다.
     *
     * @param contentType 콘텐츠 타입 문자열
     * @param id 엔트리 ID
     * @return 조합 키 문자열
     */
    private String toKey(final String contentType, final Integer id) {
        return String.format("%s:%d", StringUtils.defaultString(contentType), id);
    }
}
