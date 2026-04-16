package io.nicheblog.dreamdiary.feature.journal.annual.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.annual.entity.JournalAnnualReviewEntity;
import io.nicheblog.dreamdiary.feature.journal.annual.mapstruct.JournalAnnualReviewMapstruct;
import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualReviewDto;
import io.nicheblog.dreamdiary.feature.journal.annual.repository.jpa.JournalAnnualReviewRepository;
import io.nicheblog.dreamdiary.feature.journal.annual.spec.JournalAnnualReviewSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JournalAnnualReviewService
 * <pre>
 *  저널 결산 리뷰 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalAnnualReviewService
        implements BaseClsfService<JournalAnnualReviewDto, JournalAnnualReviewDto, Integer, JournalAnnualReviewEntity>, BaseMultipartWritableService<JournalAnnualReviewDto, JournalAnnualReviewDto, Integer, JournalAnnualReviewEntity> {

    @Getter
    private final JournalAnnualReviewRepository repository;
    @Getter
    private final JournalAnnualReviewSpec spec;
    @Getter
    private final JournalAnnualReviewMapstruct mapstruct;

    public JournalAnnualReviewMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public JournalAnnualReviewMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalCacheEvictWorker journalCacheEvictWorker;

    private final ApplicationContext context;

    private JournalAnnualReviewService getSelf() {
        return context.getBean(this.getClass());
    }

    public List<JournalAnnualReviewDto> getListDtoByUser(final String username, final io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalAnnualReviewDto updatedDto) throws Exception {
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_ANNUAL_REVIEW);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalAnnualReviewDto postDto, final JournalAnnualReviewDto updatedDto) throws Exception {
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_ANNUAL_REVIEW);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체. (dto)
     * @param modifyEntity - 수정할 객체. (entity)
     */
    @Override
    public void preModify(final JournalAnnualReviewDto modifyDto, final JournalAnnualReviewEntity modifyEntity) throws Exception {
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
    public void preDelete(final JournalAnnualReviewDto deletedDto) throws Exception {
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
    public void postDelete(final JournalAnnualReviewDto deletedDto) throws Exception {
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_ANNUAL_REVIEW);
    }
}


