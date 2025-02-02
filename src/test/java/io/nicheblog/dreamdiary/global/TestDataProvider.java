package io.nicheblog.dreamdiary.global;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

/**
 * TestDataProvider
 * <pre>
 *  경계값 테스트를 위한 테스트 파라미터 데이터 팩토리 모듈
 * </pre>
 * 
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class TestDataProvider {

    // 경계값 테스트를 위한 메서드
    public static Stream<Arguments> boundaryDateProvider() {
        return Stream.of(
                Arguments.of("2000-01-01"),  // 정상
                Arguments.of("1999-12-31"),  // 정상
                Arguments.of("2020-02-29"),  // 윤년 정상
                Arguments.of("2021-02-29"),  // 비윤년 잘못된 날짜
                Arguments.of("2020-01-01"),  // 중복 날짜 확인
                Arguments.of("2000-01-32")   // 잘못된 날짜
        );
    }

}
