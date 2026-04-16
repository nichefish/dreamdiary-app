package io.nicheblog.dreamdiary.feature.board.notice.service;

import io.nicheblog.dreamdiary.feature.board.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.feature.board.notice.mapstruct.NoticeMapstruct;
import io.nicheblog.dreamdiary.feature.board.notice.model.NoticeDto;
import io.nicheblog.dreamdiary.feature.board.notice.model.NoticeSearchParam;
import io.nicheblog.dreamdiary.feature.board.notice.model.NoticeXlsxDto;
import io.nicheblog.dreamdiary.feature.board.notice.repository.jpa.NoticeRepository;
import io.nicheblog.dreamdiary.feature.board.notice.spec.NoticeSpec;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.attachable.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.stream.Stream;

/**
 * NoticeService
 * <pre>
 *  공지사항 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class NoticeService
        implements BaseAttachableService<NoticeDto, NoticeDto, Integer, NoticeEntity>, BaseMultipartWritableService<NoticeDto, NoticeDto, Integer, NoticeEntity> {

    @Getter
    private final NoticeRepository repository;
    @Getter
    private final NoticeSpec spec;
    @Getter
    private final NoticeMapstruct mapstruct = NoticeMapstruct.INSTANCE;

    public NoticeMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public NoticeMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 최종수정일이 조회기준일자 이내이고, 최종수정자(또는 작성자)가 내가 아니고, 내가 (수정 이후로) 조회하지 않은 글 갯수를 조회한다.
     *
     * @param username 사용자 계정명
     * @param stdrdDt 조회기준일자 (ex.1주일)
     * @return Integer
     */
    @Transactional(readOnly = true)
    public Integer getUnreadCnt(final @Param("username") String username, final @Param("stdrdDt") Date stdrdDt) {
        return repository.getUnreadCnt(username, stdrdDt);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final NoticeDto updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getAttachableKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyNoticeReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * default: 상세 페이지 조회
     *
     * @param key 조회수를 증가시킬 항목의 키
     * @return Dto -- 조회된 객체
     */
    @Transactional
    public NoticeDto viewDtlPage(final Integer key) throws Exception {

        // 조회수 증가
        // this.hitCntUp(key);

        return this.getDtlDto(key);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final NoticeDto postDto,  NoticeDto updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getAttachableKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyNoticeReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 엑셀 다운로드 스트림 조회.
     *
     * @param searchParam 검색 파라미터 객체
     * @return {@link Stream} -- 변환된 Dto 스트림
     */
    @Transactional(readOnly = true)
    public Stream<NoticeXlsxDto> getStreamXlsxDto(NoticeSearchParam searchParam) throws Exception {
        final Stream<NoticeEntity> entityStream = this.getStreamEntity(searchParam);

        return entityStream.map(e -> {
            try {
                return mapstruct.toXlsxDto(e);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}

