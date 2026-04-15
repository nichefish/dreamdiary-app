package io.nicheblog.dreamdiary.feature.journal.sbjct.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.clsf.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.feature.journal.sbjct.entity.JournalSbjctEntity;
import io.nicheblog.dreamdiary.feature.journal.sbjct.mapstruct.JournalSbjctMapstruct;
import io.nicheblog.dreamdiary.feature.journal.sbjct.model.JournalSbjctDto;
import io.nicheblog.dreamdiary.feature.journal.sbjct.repository.jpa.JournalSbjctRepository;
import io.nicheblog.dreamdiary.feature.journal.sbjct.spec.JournalSbjctSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalSbjctService
 * <pre>
 *  저널 주제 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("journalSbjctService")
@RequiredArgsConstructor
public class JournalSbjctService
        implements BaseClsfService<JournalSbjctDto, JournalSbjctDto, Integer, JournalSbjctEntity>, BaseMultipartWritableService<JournalSbjctDto, JournalSbjctDto, Integer, JournalSbjctEntity> {

    @Getter
    private final JournalSbjctRepository repository;
    @Getter
    private final JournalSbjctSpec spec;
    @Getter
    private final JournalSbjctMapstruct mapstruct = JournalSbjctMapstruct.INSTANCE;
    public JournalSbjctMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalSbjctMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalSbjctDto updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyJournalSbjctReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 상세 페이지 조회 후처리 (dto level)
     *
     * @param key - 조회된 Dto 객체
     */
    @Transactional
    public JournalSbjctDto viewDtlPage(final Integer key) throws Exception {
        return this.getDtlDto(key);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - ?섏젙??媛앹껜
     * @param modifyEntity - ?섏젙??媛앹껜
     */
    @Override
    public void preModify(final JournalSbjctDto modifyDto, final JournalSbjctEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalSbjctDto postDto, final JournalSbjctDto updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyJournalSbjctReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제될 객체
     */
    @Override
    public void preDelete(final JournalSbjctDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }
}

