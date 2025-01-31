package io.nicheblog.dreamdiary.domain.notice.service;

import io.nicheblog.dreamdiary.domain.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.domain.notice.mapstruct.NoticeMapstruct;
import io.nicheblog.dreamdiary.domain.notice.model.NoticeDto;
import io.nicheblog.dreamdiary.domain.notice.model.NoticeSearchParam;
import io.nicheblog.dreamdiary.domain.notice.model.NoticeXlsxDto;
import io.nicheblog.dreamdiary.domain.notice.repository.jpa.NoticeRepository;
import io.nicheblog.dreamdiary.domain.notice.spec.NoticeSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BasePostService;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.stream.Stream;

/**
 * NoticeService
 * <pre>
 *  공지사항 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface NoticeService
        extends BasePostService<NoticeDto.DTL, NoticeDto.LIST, Integer, NoticeEntity, NoticeRepository, NoticeSpec, NoticeMapstruct> {

    /**
     * 최종수정일이 조회기준일자 이내이고, 최종수정자(또는 작성자)가 내가 아니고, 내가 (수정 이후로) 조회하지 않은 글 갯수를 조회한다.
     *
     * @param userId 사용자 ID
     * @param stdrdDt 조회기준일자 (ex.1주일)
     * @return Integer
     */
    Integer getUnreadCnt(final @Param("userId") String userId, final @Param("stdrdDt") Date stdrdDt);
    
    /**
     * 엑셀 다운로드 스트림 조회.
     *
     * @param searchParam 검색 파라미터 객체
     * @return {@link Stream} -- 변환된 Dto 스트림
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    Stream<NoticeXlsxDto> getStreamXlsxDto(NoticeSearchParam searchParam) throws Exception;
}