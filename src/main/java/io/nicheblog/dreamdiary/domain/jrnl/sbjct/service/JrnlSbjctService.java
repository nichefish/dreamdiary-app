package io.nicheblog.dreamdiary.domain.jrnl.sbjct.service;

import io.nicheblog.dreamdiary.domain.jrnl.sbjct.entity.JrnlSbjctEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.mapstruct.JrnlSbjctMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.repository.jpa.JrnlSbjctRepository;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.spec.JrnlSbjctSpec;
import io.nicheblog.dreamdiary.extension.clsf.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;
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
        implements BaseClsfService<JrnlSbjctDto, JrnlSbjctDto, Integer, JrnlSbjctEntity> {

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
     * 단일 항목 조회 (dto level)
     *
     * @param key 조회할 엔티티의 키
     * @return {@link JrnlSbjctDto} -- 조회 항목 반환
     */
    @Transactional(readOnly = true)
    public JrnlSbjctDto getDtlDto(final Integer key) throws Exception {
        final JrnlSbjctEntity retrievedEntity = this.getDtlEntity(key);

        return mapstruct.toDto(retrievedEntity);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlSbjctDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
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
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
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
     */
    @Override
    public void postDelete(final JrnlSbjctDto deletedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, deletedDto.getClsfKey()));
    }
}