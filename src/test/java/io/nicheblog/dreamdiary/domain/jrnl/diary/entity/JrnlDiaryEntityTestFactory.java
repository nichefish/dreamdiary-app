package io.nicheblog.dreamdiary.domain.jrnl.diary.entity;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlDiaryEntityTestFactory
 * <pre>
 *  저널 일기 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlDiaryEntityTestFactory {

    /**
     * 테스트용 저널 일기 Entity 생성
     * @param jrnlDayEntity 저널 일자 Entity 객체
     */
    public static JrnlDiaryEntity createWithJrnlDay(JrnlDayEntity jrnlDayEntity) throws Exception {
        return JrnlDiaryEntity.builder()
                .contentType(ContentType.JRNL_DIARY.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .jrnlDay(JrnlDaySmpEntity.from(jrnlDayEntity))
                .build();
    }

    /**
     * 테스트용 저널 일기 Entity 생성
     * @param jrnlDtStr 저널 일자 날짜 문자열
     */
    public static JrnlDiaryEntity createWithJrnlDt(String jrnlDtStr) throws Exception {
        return JrnlDiaryEntity.builder()
                .contentType(ContentType.JRNL_DIARY.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .jrnlDay(JrnlDaySmpEntity.builder().jrnlDt(DateUtils.asDate(jrnlDtStr)).build())
                .build();
    }

    /**
     * 테스트용 저널 일기 Entity 생성
     */
    public static JrnlDiaryEntity create() throws Exception {
        String tempJrnlDtStr = "2000-01-01";
        return createWithJrnlDt(tempJrnlDtStr);
    }
}
