package io.nicheblog.dreamdiary.domain.board.notice.mapstuct;

import io.nicheblog.dreamdiary.domain.board.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.domain.board.notice.entity.NoticeEntityTestFactory;
import io.nicheblog.dreamdiary.domain.board.notice.mapstruct.NoticeMapstruct;
import io.nicheblog.dreamdiary.domain.board.notice.model.NoticeDto;
import io.nicheblog.dreamdiary.domain.board.notice.model.NoticeDtoTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * NoticeMapstructTest
 * <pre>
 *  공지사항 Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class NoticeMapstructTest {

    private final NoticeMapstruct noticeMapstruct = NoticeMapstruct.INSTANCE;

    /**
     * toDto 검증 :: 등록자/수정자 정보
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        NoticeEntity noticeEntity = NoticeEntityTestFactory.create();
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setRegstrInfo(noticeEntity);
        BaseEntityTestFactoryHelper.setMdfusrInfo(noticeEntity);

        // When::
        NoticeDto noticeDto = noticeMapstruct.toDto(noticeEntity);

        // Then::
        assertNotNull(noticeDto, "변환된 공지사항 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, noticeDto.getRegstrId(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, noticeDto.getRegstrNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", noticeDto.getRegDt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, noticeDto.getMdfusrId(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, noticeDto.getMdfusrNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", noticeDto.getMdfDt(), "수정일시가 제대로 매핑되지 않았습니다.");
    }

    /* ----- */

    /**
     * dto -> entity 변환 검증
     */
    @Test
    void testToEntity_checkBasic() throws Exception {
        // Given::
        NoticeDto.DTL noticeDto = NoticeDtoTestFactory.create();

        // When::
        NoticeEntity noticeEntity = noticeMapstruct.toEntity(noticeDto);

        // Then::
        assertNotNull(noticeEntity, "변환된 공지사항 Entity는 null일 수 없습니다.");
    }

    /* ----- */

    /**
     * updateFromDto 검증 :: 기본 속성
     * TODO
     */
    @Test
    void testUpdateFromDto_checkBasic() throws Exception {
        //
    }
}