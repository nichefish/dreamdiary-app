package io.nicheblog.dreamdiary.feature.journal.thread.mapstuct;

import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbed;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.thread.mapstruct.JournalThreadMapstruct;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * JournalThreadMapstructTest
 * <pre>
 *  저널 스레드  Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class JournalThreadMapstructTest {

    private static final int FIXTURE_REQUEST_PREFIX_ID = 101;
    private static final int FIXTURE_EXISTING_PREFIX_ID = 202;
    private static final String FIXTURE_REQUEST_PREFIX_NAME = "업무";
    private static final String FIXTURE_EXISTING_PREFIX_NAME = "개인";
    private static final String FIXTURE_THREAD_TITLE = "테스트 스레드";

    private final JournalThreadMapstruct journalThreadMapstruct = JournalThreadMapstruct.INSTANCE;

    private JournalThreadEntity journalThreadEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JournalThreadEntity 초기화
        journalThreadEntity = JournalThreadEntityTestFactory.create();    // 2000년 1월 1일, 툐요일.
    }

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final JournalThreadDto journalThreadDto = journalThreadMapstruct.toDto(journalThreadEntity);

        // Then::
        assertNotNull(journalThreadDto, "변환된 저널 스레드 Dto는 null일 수 없습니다.");
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 체크
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setCreatedByInfo(journalThreadEntity);
        BaseEntityTestFactoryHelper.setUpdatedByInfo(journalThreadEntity);

        // When::
        final JournalThreadDto journalThreadDto = journalThreadMapstruct.toDto(journalThreadEntity);

        // Then::
        assertNotNull(journalThreadDto, "변환된 저널 일기 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, journalThreadDto.getCreatedBy(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, journalThreadDto.getCreatedByNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalThreadDto.getCreatedAt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, journalThreadDto.getUpdatedBy(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, journalThreadDto.getUpdatedByNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalThreadDto.getUpdatedAt(), "수정일시가 제대로 매핑되지 않았습니다.");
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

    /**
     * 쓰기 매핑은 검증 전 중첩 Prefix DTO를 영속 관계에 반영하지 않는다.
     */
    @Test
    void testWriteMappingIgnoresNestedPrefix() throws Exception {
        // Given::
        final PrefixDto requestPrefix = PrefixDto.builder()
                .id(FIXTURE_REQUEST_PREFIX_ID)
                .name(FIXTURE_REQUEST_PREFIX_NAME)
                .build();
        final JournalThreadDto dto = JournalThreadDto.builder()
                .title(FIXTURE_THREAD_TITLE)
                .prefix(requestPrefix)
                .prefixId(FIXTURE_REQUEST_PREFIX_ID)
                .build();
        // 콘텐츠는 prefix FK를 직접 들지 않고 PrefixEmbed(prefix_content 연결)를 물린다.
        final PrefixEmbed existingPrefix = PrefixEmbed.builder().build();
        journalThreadEntity.setPrefix(existingPrefix);

        // When::
        final JournalThreadEntity created = journalThreadMapstruct.toEntity(dto);
        journalThreadMapstruct.updateFromDto(dto, journalThreadEntity);

        // Then::
        assertNull(created.getPrefix(), "등록 쓰기 매핑은 중첩 Prefix DTO를 임베드로 변환하지 않아야 합니다.");
        assertSame(existingPrefix, journalThreadEntity.getPrefix(), "수정 쓰기 매핑은 검증 전 기존 Prefix 임베드를 변경하지 않아야 합니다.");
    }
}
