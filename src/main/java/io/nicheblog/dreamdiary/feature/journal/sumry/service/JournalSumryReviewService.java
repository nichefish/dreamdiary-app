package io.nicheblog.dreamdiary.feature.journal.sumry.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.sumry.entity.JournalSumryReviewEntity;
import io.nicheblog.dreamdiary.feature.journal.sumry.mapstruct.JournalSumryReviewMapstruct;
import io.nicheblog.dreamdiary.feature.journal.sumry.model.JournalSumryReviewDto;
import io.nicheblog.dreamdiary.feature.journal.sumry.repository.jpa.JournalSumryReviewRepository;
import io.nicheblog.dreamdiary.feature.journal.sumry.spec.JournalSumryReviewSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JournalSumryReviewService
 * <pre>
 *  저널 결산 리뷰 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("journalSumryReviewService")
@RequiredArgsConstructor
@Log4j2
public class JournalSumryReviewService
        implements BaseClsfService<JournalSumryReviewDto, JournalSumryReviewDto, Integer, JournalSumryReviewEntity>, BaseMultipartWritableService<JournalSumryReviewDto, JournalSumryReviewDto, Integer, JournalSumryReviewEntity> {

    @Getter
    private final JournalSumryReviewRepository repository;
    @Getter
    private final JournalSumryReviewSpec spec;
    @Getter
    private final JournalSumryReviewMapstruct mapstruct;

    public JournalSumryReviewMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public JournalSumryReviewMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalCacheEvictWorker journalCacheEvictWorker;

    private final ApplicationContext context;

    private JournalSumryReviewService getSelf() {
        return context.getBean(this.getClass());
    }

    public List<JournalSumryReviewDto> getListDtoByUser(final String username, final io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalSumryReviewDto updatedDto) throws Exception {
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_SUMRY_REVIEW);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalSumryReviewDto postDto, final JournalSumryReviewDto updatedDto) throws Exception {
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_SUMRY_REVIEW);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체. (dto)
     * @param modifyEntity - 수정할 객체. (entity)
     */
    @Override
    public void preModify(final JournalSumryReviewDto modifyDto, final JournalSumryReviewEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 등록된 객체
     */
    @Override
    public void preDelete(final JournalSumryReviewDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 등록된 객체
     */
    @Override
    public void postDelete(final JournalSumryReviewDto deletedDto) throws Exception {
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_SUMRY_REVIEW);
    }
}

