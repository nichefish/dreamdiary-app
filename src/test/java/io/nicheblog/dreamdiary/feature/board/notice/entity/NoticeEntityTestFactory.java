package io.nicheblog.dreamdiary.feature.board.notice.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * NoticeEntityTestFactory
 * <pre>
 *  공지사항 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class NoticeEntityTestFactory {

    /**
     * 테스트용 공지사항 Entity 생성
     */
    public static NoticeEntity create() throws Exception {
        return NoticeEntity.builder()
                .id(0)
                .contentType(ContentType.NOTICE.key)
                .popupYn("Y")
                .title("test_title")
                .content("test_cn")
                .ctgrCd("test_ctgr_cd")
                .build();
    }
}
