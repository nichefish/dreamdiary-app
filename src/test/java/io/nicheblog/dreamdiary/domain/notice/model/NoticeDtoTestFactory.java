package io.nicheblog.dreamdiary.domain.notice.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * NoticeDtoTestFactory
 * <pre>
 *  공지사항 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class NoticeDtoTestFactory {

    /**
     * 테스트용 공지사항 상세 Dto 생성
     */
    public static NoticeDto.DTL create() throws Exception {
        return NoticeDto.DTL.builder()
                .postNo(0)
                .contentType(ContentType.NOTICE.key)
                .popupYn("Y")
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .build();
    }
}
