package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.Objects;

@Component
public class JournalEntryPolicyResolver {

    /**
     * DTO의 콘텐츠 타입으로 정책을 해석한다.
     *
     * @param dto attachable DTO
     * @return 엔트리 타입 정책
     */
    public JournalEntryTypePolicy resolve(final BaseAttachableDto dto) {
        return resolve(dto != null ? dto.getContentType() : null);
    }

    /**
     * 문자열 콘텐츠 타입으로 정책을 해석한다.
     *
     * @param contentType 콘텐츠 타입 문자열
     * @return 엔트리 타입 정책
     */
    public JournalEntryTypePolicy resolve(final String contentType) {
        return resolve(ContentType.get(contentType));
    }

    /**
     * enum 콘텐츠 타입으로 정책을 해석한다.
     *
     * @param contentType 콘텐츠 타입 enum
     * @return 엔트리 타입 정책
     */
    public JournalEntryTypePolicy resolve(final ContentType contentType) {
        if (!JournalEntryTypePolicy.isEntryType(contentType)) {
            throw new IllegalArgumentException("contentType is required for journal entry operation.");
        }
        return JournalEntryTypePolicy.from(contentType);
    }

    /**
     * 문자열 콘텐츠 타입이 엔트리 타입인지 확인한다.
     *
     * @param contentType 콘텐츠 타입 문자열
     * @return 엔트리 타입 여부
     */
    public boolean isEntryType(final String contentType) {
        return StringUtils.isNotBlank(contentType)
                && JournalEntryTypePolicy.isEntryType(ContentType.get(contentType));
    }

    /**
     * 엔티티의 콘텐츠 타입이 정책과 일치하는지 검증한다.
     *
     * @param entity 조회 엔티티
     * @param policy 정책
     */
    public void assertMatches(final JournalEntryEntity entity, final JournalEntryTypePolicy policy) {
        if (entity == null || !Objects.equals(entity.getContentType(), policy.contentType.key)) {
            throw new EntityNotFoundException();
        }
    }
}
