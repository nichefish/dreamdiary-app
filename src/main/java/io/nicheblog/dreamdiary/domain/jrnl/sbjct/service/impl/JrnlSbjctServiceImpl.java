package io.nicheblog.dreamdiary.domain.jrnl.sbjct.service.impl;

import io.nicheblog.dreamdiary.domain.jrnl.sbjct.mapstruct.JrnlSbjctMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.repository.jpa.JrnlSbjctRepository;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.service.JrnlSbjctService;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.spec.JrnlSbjctSpec;
import io.nicheblog.dreamdiary.extension.clsf.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.viewer.event.ViewerAddEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
public class JrnlSbjctServiceImpl
        implements JrnlSbjctService {

    @Getter
    private final JrnlSbjctRepository repository;
    @Getter
    private final JrnlSbjctSpec spec;
    @Getter
    private final JrnlSbjctMapstruct mapstruct = JrnlSbjctMapstruct.INSTANCE;

    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final JrnlSbjctDto.DTL updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishAsyncEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishAsyncEventAndWait(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyJrnlSbjctReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 상세 페이지 조회 후처리 (dto level)
     *
     * @param retrievedDto - 조회된 Dto 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postViewDtlPage(final JrnlSbjctDto.DTL retrievedDto) throws Exception {
        // 열람자 추가 :: 메인 로직과 분리
        publisher.publishAsyncEvent(new ViewerAddEvent(this, retrievedDto.getClsfKey()));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final JrnlSbjctDto.DTL updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishAsyncEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishAsyncEventAndWait(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyJrnlSbjctReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postDelete(final JrnlSbjctDto.DTL deletedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishAsyncEventAndWait(new TagProcEvent(this, deletedDto.getClsfKey()));
    }
}