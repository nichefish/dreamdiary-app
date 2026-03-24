package io.nicheblog.dreamdiary.feature.jrnl.sbjct.service;

import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.clsf.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.entity.JrnlSbjctEntity;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.mapstruct.JrnlSbjctMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.repository.jpa.JrnlSbjctRepository;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.spec.JrnlSbjctSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JrnlSbjctService
 * <pre>
 *  저널 주제 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlSbjctService")
@RequiredArgsConstructor
public class JrnlSbjctService
        implements BaseClsfService<JrnlSbjctDto, JrnlSbjctDto, Integer, JrnlSbjctEntity>, BaseMultipartWritableService<JrnlSbjctDto, JrnlSbjctDto, Integer, JrnlSbjctEntity> {

    @Getter
    private final JrnlSbjctRepository repository;
    @Getter
    private final JrnlSbjctSpec spec;
    @Getter
    private final JrnlSbjctMapstruct mapstruct = JrnlSbjctMapstruct.INSTANCE;
    public JrnlSbjctMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlSbjctMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlSbjctDto updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyJrnlSbjctReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 상세 페이지 조회 후처리 (dto level)
     *
     * @param key - 조회된 Dto 객체
     */
    @Transactional
    public JrnlSbjctDto viewDtlPage(final Integer key) throws Exception {
        return this.getDtlDto(key);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlSbjctDto postDto, final JrnlSbjctDto updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyJrnlSbjctReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }
}
