package io.nicheblog.dreamdiary.domain.board.notice.service.impl;

import io.nicheblog.dreamdiary.domain.board.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.domain.board.notice.mapstruct.NoticeMapstruct;
import io.nicheblog.dreamdiary.domain.board.notice.model.NoticeDto;
import io.nicheblog.dreamdiary.domain.board.notice.model.NoticeSearchParam;
import io.nicheblog.dreamdiary.domain.board.notice.model.NoticeXlsxDto;
import io.nicheblog.dreamdiary.domain.board.notice.repository.jpa.NoticeRepository;
import io.nicheblog.dreamdiary.domain.board.notice.service.NoticeService;
import io.nicheblog.dreamdiary.domain.board.notice.spec.NoticeSpec;
import io.nicheblog.dreamdiary.extension.clsf.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.viewer.event.ViewerAddEvent;
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
@Service("noticeService")
@RequiredArgsConstructor
@Log4j2
public class NoticeServiceImpl
        implements NoticeService {

    @Getter
    private final NoticeRepository repository;
    @Getter
    private final NoticeSpec spec;
    @Getter
    private final NoticeMapstruct mapstruct = NoticeMapstruct.INSTANCE;

    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 최종수정일이 조회기준일자 이내이고, 최종수정자(또는 작성자)가 내가 아니고, 내가 (수정 이후로) 조회하지 않은 글 갯수를 조회한다.
     *
     * @param userId 사용자 ID
     * @param stdrdDt 조회기준일자 (ex.1주일)
     * @return Integer
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getUnreadCnt(final @Param("userId") String userId, final @Param("stdrdDt") Date stdrdDt) {
        return repository.getUnreadCnt(userId, stdrdDt);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final NoticeDto.DTL updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyNoticeReg(trgetTopic, result, logParam);
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
    public void postViewDtlPage(final NoticeDto.DTL retrievedDto) throws Exception {
        // 열람자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ViewerAddEvent(this, retrievedDto.getClsfKey()));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final NoticeDto.DTL updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyNoticeReg(trgetTopic, result, logParam);
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
    public void postDelete(final NoticeDto.DTL deletedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, deletedDto.getClsfKey()));
    }
    
    /**
     * 엑셀 다운로드 스트림 조회.
     *
     * @param searchParam 검색 파라미터 객체
     * @return {@link Stream} -- 변환된 Dto 스트림
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
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