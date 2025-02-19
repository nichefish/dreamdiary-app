package io.nicheblog.dreamdiary.domain.user.profl.mapstuct;

import io.nicheblog.dreamdiary.domain.user.info.model.profl.UserProflDto;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntity;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntityTestFactory;
import io.nicheblog.dreamdiary.domain.user.profl.mapstruct.UserProflMapstruct;
import io.nicheblog.dreamdiary.domain.user.profl.model.UserProflDtoTestFactory;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UserProflMapstructTest
 * <pre>
 *  사용자 프로필 정보 Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class UserProflMapstructTest {

    private final UserProflMapstruct userProflMapstruct = UserProflMapstruct.INSTANCE;

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkProfl() throws Exception {

        // Given::
        final UserProflEntity userProflEntity = UserProflEntityTestFactory.create();

        // When::
        final UserProflDto userProflDto = userProflMapstruct.toDto(userProflEntity);

        // Then::
        assertNotNull(userProflDto, "변환된 프로필 정보 Dto는 null일 수 없습니다.");
        // 날짜 변환 체크
        assertEquals("2000-01-01", userProflDto.getBrthdy(), "프로필 생일 정보가 제대로 매핑되지 않았습니다.");
    }

    /* ----- */

    /**
     * dto -> entity 변환 검증
     */
    @Test
    void testToEntity_checkBasic() throws Exception {
        // Given::
        final UserProflDto userProflDto = UserProflDtoTestFactory.create();

        // When::
        final UserProflEntity userProflEntity = userProflMapstruct.toEntity(userProflDto);

        // Then::
        assertNotNull(userProflEntity, "변환된 프로필 정보 Entity는 null일 수 없습니다.");
        // 날짜 변환 체크
        assertEquals(DateUtils.asDate("2000-01-01"), userProflEntity.getBrthdy(), "프로필 생일 정보가 제대로 매핑되지 않았습니다.");
    }

}