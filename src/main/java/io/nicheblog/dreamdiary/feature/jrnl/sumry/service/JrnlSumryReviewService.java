package io.nicheblog.dreamdiary.feature.jrnl.sumry.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.entity.JrnlSumryReviewEntity;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.mapstruct.JrnlSumryReviewMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumryReviewDto;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.repository.jpa.JrnlSumryReviewRepository;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.spec.JrnlSumryReviewSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JrnlSumryReviewService
 * <pre>
 *  저널 결산 리뷰 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlSumryReviewService")
@RequiredArgsConstructor
@Log4j2
public class JrnlSumryReviewService
        implements BaseClsfService<JrnlSumryReviewDto, JrnlSumryReviewDto, Integer, JrnlSumryReviewEntity>, BaseMultipartWritableService<JrnlSumryReviewDto, JrnlSumryReviewDto, Integer, JrnlSumryReviewEntity> {

    @Getter
    private final JrnlSumryReviewRepository repository;
    @Getter
    private final JrnlSumryReviewSpec spec;
    @Getter
    private final JrnlSumryReviewMapstruct mapstruct;

    public JrnlSumryReviewMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public JrnlSumryReviewMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

    private final ApplicationContext context;

    private JrnlSumryReviewService getSelf() {
        return context.getBean(this.getClass());
    }

    public List<JrnlSumryReviewDto> getListDtoByUser(final String userId, final io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlSumryReviewDto updatedDto) throws Exception {
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_SUMRY_REVIEW);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlSumryReviewDto postDto, final JrnlSumryReviewDto updatedDto) throws Exception {
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_SUMRY_REVIEW);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체. (dto)
     * @param modifyEntity - 수정할 객체. (entity)
     */
    @Override
    public void preModify(final JrnlSumryReviewDto modifyDto, final JrnlSumryReviewEntity modifyEntity) throws Exception {
        if (!AuthUtils.isRegstr(modifyEntity.getRegstrId())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 등록된 객체
     */
    @Override
    public void preDelete(final JrnlSumryReviewDto deletedDto) throws Exception {
        if (!AuthUtils.isRegstr(deletedDto.getRegstrId())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 등록된 객체
     */
    @Override
    public void postDelete(final JrnlSumryReviewDto deletedDto) throws Exception {
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_SUMRY_REVIEW);
    }
}
