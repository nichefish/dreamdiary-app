package io.nicheblog.dreamdiary.domain.jrnl.diary.model;

import io.nicheblog.dreamdiary.domain.clsf.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlDiaryDtoTestFactory
 * <pre>
 *  저널 일기 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlDiaryDtoTestFactory {

    /**
     * 테스트용 저널 일기 Dto 생성
     */
    public static JrnlDiaryDto create() throws Exception {
        return JrnlDiaryDto.builder()
                .contentType(ContentType.JRNL_DIARY.key)
                .build();
    }

    /**
     * 테스트용 저널 일기 Dto 생성
     * @param key 식별자
     */
    public static JrnlDiaryDto createWithKey(final Integer key) throws Exception {
        return JrnlDiaryDto.builder()
                .postNo(key)
                .contentType(ContentType.JRNL_DIARY.key)
                .build();
    }

    /**
     * 테스트용 일기 PostDto 생성
     */
    public static JrnlDiaryPostDto createPost() throws Exception {
        return JrnlDiaryPostDto.builder()
                .contentType(ContentType.JRNL_DIARY.key)
                .build();
    }

    /**
     * 테스트용 일기 PostDto 생성
     * @param key 식별키
     */
    public static JrnlDiaryPostDto createPostWithKey(final Integer key) throws Exception {
        return JrnlDiaryPostDto.builder()
                .postNo(key)
                .contentType(ContentType.JRNL_DIARY.key)
                .build();
    }
}
