package io.nicheblog.dreamdiary.feature.journal.thread.spec;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity.LifecycleEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntryEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JournalThreadSpec
 * <pre>
 *  게시판 게시물 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class JournalThreadSpec
        implements BaseAttachableSpec<JournalThreadEntity> {

    /** 소속 엔트리 태그가 실릴 수 있는 contentType 키. */
    private static final List<String> MEMBER_ENTRY_TAG_CONTENT_TYPES = List.of(
            ContentType.JOURNAL_DIARY.key,
            ContentType.JOURNAL_DREAM.key,
            ContentType.JOURNAL_NOTE.key
    );

    /**
     * 인자별로 구체적인 검색 조건을 세팅한다. (override)
     *
     * @param searchParamMap 검색 파라미터 맵
     * @param root 검색할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     */
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<JournalThreadEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        // expressions
        final Expression<LocalDateTime> createdAtExp = root.get("createdAt");

        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
            final Object value = searchParamMap.get(key);
            switch (key) {
                case "searchStartDt":
                    // 기간 검색
                    predicate.add(builder.greaterThanOrEqualTo(createdAtExp, DateUtils.asLocalDateTime(value)));
                    continue;
                case "searchEndDt":
                    // 기간 검색
                    predicate.add(builder.lessThanOrEqualTo(createdAtExp, DateUtils.asLocalDateTime(value)));
                    continue;
                case "board":
                    // board를 contentType으로 이용
                    predicate.add(builder.equal(root.get("contentType"), value));
                    continue;
                case "searchKeyword":
                    // 제목 키워드 (searchType=title 과 함께 전달). 빈 값은 무시.
                    if (value == null || StringUtils.isBlank(value.toString())) continue;
                    predicate.add(builder.like(
                            builder.lower(root.get("title")),
                            "%" + value.toString().trim().toLowerCase() + "%"
                    ));
                    continue;
                case "searchType":
                    // searchKeyword 와 한 세트. 자체 Predicate 없음.
                    continue;
                case "tagIds":
                    // 소속 엔트리 태그 합집합 AND 필터
                    resolveMemberEntryTagIdsPredicate(predicate, root, query, builder, value);
                    continue;
                case "lifecycleKey":
                    // 스레드 부착 라이프사이클. OPEN 은 행 없음 포함.
                    resolveLifecycleKeyPredicate(predicate, root, query, builder, value);
                    continue;
                default:
                    // default :: 조건 파라미터에 대해 equal 검색
                    try {
                        predicate.add(builder.equal(root.get(key), value));
                    } catch (final Exception e) {
                        log.info("unable to locate attribute '{}' while trying root.get(key).", key);
                    }
            }
        }

        return predicate;
    }

    /**
     * 소속 엔트리 태그 합집합이 선택한 tagId 를 모두 포함하는 스레드만 남긴다 (AND).
     * <p>
     * 한 엔트리가 모든 태그를 가질 필요는 없다. 스레드 소속 엔트리들 전체에서
     * distinct tagId 가 필터 집합을 덮으면 통과한다 — 목록 행의 집계 태그 표시와 동일 의미다.
     * </p>
     *
     * @param predicate 누적 Predicate
     * @param root 스레드 Root
     * @param query CriteriaQuery
     * @param builder CriteriaBuilder
     * @param value tagIds 원본 (List)
     */
    private void resolveMemberEntryTagIdsPredicate(
            final List<Predicate> predicate,
            final Root<JournalThreadEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Object value
    ) {
        if (!(value instanceof List<?> rawTagList) || CollectionUtils.isEmpty(rawTagList)) return;

        final List<Integer> tagIds = new ArrayList<>();
        for (final Object raw : rawTagList) {
            if (raw == null) continue;
            if (raw instanceof Integer id) {
                tagIds.add(id);
            } else if (raw instanceof Number number) {
                tagIds.add(number.intValue());
            } else {
                final String text = raw.toString().trim();
                if (text.isEmpty()) continue;
                try {
                    tagIds.add(Integer.valueOf(text));
                } catch (final NumberFormatException e) {
                    log.warn("[JournalThreadSpec] skip non-integer tagId={}", text);
                }
            }
        }
        final List<Integer> distinctTagIds = tagIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctTagIds.isEmpty()) return;

        final String username = AuthUtils.requireLoginUsername();

        final Subquery<Integer> tagSubquery = query.subquery(Integer.class);
        final Root<JournalThreadEntryEntity> membershipRoot = tagSubquery.from(JournalThreadEntryEntity.class);
        final Root<TagContentEntity> tagRoot = tagSubquery.from(TagContentEntity.class);

        final List<Predicate> subPredicates = new ArrayList<>();
        subPredicates.add(builder.equal(membershipRoot.get("threadId"), root.get("id")));
        subPredicates.add(builder.equal(tagRoot.get("refId"), membershipRoot.get("entryId")));
        subPredicates.add(tagRoot.get("refContentType").in(MEMBER_ENTRY_TAG_CONTENT_TYPES));
        subPredicates.add(tagRoot.get("tagId").in(distinctTagIds));
        subPredicates.add(builder.equal(membershipRoot.get("createdBy"), username));
        subPredicates.add(builder.equal(tagRoot.get("createdBy"), username));

        tagSubquery.select(membershipRoot.get("threadId"));
        tagSubquery.where(builder.and(subPredicates.toArray(new Predicate[0])));
        tagSubquery.groupBy(membershipRoot.get("threadId"));
        tagSubquery.having(builder.equal(builder.countDistinct(tagRoot.get("tagId")), (long) distinctTagIds.size()));
        predicate.add(builder.exists(tagSubquery));
    }

    /**
     * 스레드 라이프사이클 필터.
     * <p>
     * {@code OPEN} 은 lifecycle 행이 없거나 키가 OPEN 인 경우를 포함한다.
     * {@code PENDING}/{@code RESOLVED} 는 해당 키 행이 있는 스레드만 남긴다.
     * </p>
     */
    private void resolveLifecycleKeyPredicate(
            final List<Predicate> predicate,
            final Root<JournalThreadEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Object value
    ) {
        if (value == null || StringUtils.isBlank(value.toString())) return;
        final String lifecycleKey = value.toString().trim();

        final Subquery<Integer> exactSubquery = query.subquery(Integer.class);
        final Root<LifecycleEntity> exactRoot = exactSubquery.from(LifecycleEntity.class);
        exactSubquery.select(exactRoot.get("refId"));
        exactSubquery.where(
                builder.equal(exactRoot.get("refId"), root.get("id")),
                builder.equal(exactRoot.get("refContentType"), ContentType.JOURNAL_THREAD.key),
                builder.equal(exactRoot.get("lifecycleKey"), lifecycleKey)
        );

        if ("OPEN".equals(lifecycleKey)) {
            final Subquery<Integer> anySubquery = query.subquery(Integer.class);
            final Root<LifecycleEntity> anyRoot = anySubquery.from(LifecycleEntity.class);
            anySubquery.select(anyRoot.get("refId"));
            anySubquery.where(
                    builder.equal(anyRoot.get("refId"), root.get("id")),
                    builder.equal(anyRoot.get("refContentType"), ContentType.JOURNAL_THREAD.key)
            );
            predicate.add(builder.or(
                    builder.not(builder.exists(anySubquery)),
                    builder.exists(exactSubquery)
            ));
            return;
        }
        predicate.add(builder.exists(exactSubquery));
    }
}
