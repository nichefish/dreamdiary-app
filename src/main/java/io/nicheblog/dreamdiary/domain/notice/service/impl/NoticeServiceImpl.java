package io.nicheblog.dreamdiary.domain.notice.service.impl;

import io.nicheblog.dreamdiary.domain.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.domain.notice.mapstruct.NoticeMapstruct;
import io.nicheblog.dreamdiary.domain.notice.model.NoticeDto;
import io.nicheblog.dreamdiary.domain.notice.model.NoticeSearchParam;
import io.nicheblog.dreamdiary.domain.notice.model.NoticeXlsxDto;
import io.nicheblog.dreamdiary.domain.notice.repository.jpa.NoticeRepository;
import io.nicheblog.dreamdiary.domain.notice.service.NoticeService;
import io.nicheblog.dreamdiary.domain.notice.spec.NoticeSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BasePostService;
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

    /**
     * 최종수정일이 조회기준일자 이내이고, 최종수정자(또는 작성자)가 내가 아니고, 내가 (수정 이후로) 조회하지 않은 글 갯수를 조회한다.
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