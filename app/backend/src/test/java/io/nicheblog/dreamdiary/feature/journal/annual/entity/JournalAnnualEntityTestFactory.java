package io.nicheblog.dreamdiary.feature.journal.annual.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalAnnualEntityTestFactory
 * <pre>
 *  저널 결산 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalAnnualEntityTestFactory {

    /**
<<<<<<< HEAD
<<<<<<< HEAD
     * 테스트용 저널 결산 Entity 생성
=======
     * 테스트용 저널 일기 Entity 생성
>>>>>>> b9c6a276 (기초적인 CRUD 관련 테스트 코드 작성 중)
=======
     * 테스트용 저널 결산 Entity 생성
>>>>>>> 46bfb69e (기초적인 CRUD 관련 테스트 코드 작성 중)
     */
    public static JournalAnnualEntity create() throws Exception {
        return JournalAnnualEntity.builder()
                .contentType(ContentType.JOURNAL_ANNUAL.key)
                .title("test_title")
                .content("test_cn")
                .build();
    }
}
