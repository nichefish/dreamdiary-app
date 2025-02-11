package io.nicheblog.dreamdiary.domain.board.notice.entity;

import io.nicheblog.dreamdiary.domain.board.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
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
                .postNo(0)
                .contentType(ContentType.NOTICE.key)
                .popupYn("Y")
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .build();
    }
}
