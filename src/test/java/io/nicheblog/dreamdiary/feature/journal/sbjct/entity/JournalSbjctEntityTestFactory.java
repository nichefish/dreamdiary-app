package io.nicheblog.dreamdiary.feature.journal.sbjct.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalSbjctEntityTestFactory
 * <pre>
 *  저널 주제 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalSbjctEntityTestFactory {

    /**
     * 테스트용 저널 주제 Entity 생성
     */
    public static JournalSbjctEntity create() throws Exception {
        return JournalSbjctEntity.builder()
                .contentType(ContentType.JOURNAL_SBJCT.key)
                .title("test_title")
                .content("test_cn")
                .ctgrCd("test_ctgr_cd")
                .build();
    }
}
