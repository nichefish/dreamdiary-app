package io.nicheblog.dreamdiary.feature.journal._shared.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixContentRepository;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixService;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.JournalChapterService;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.JournalTestUserSupport;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDtoTestFactory;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 저널 챕터·엔트리와 개인 Prefix 연결의 실제 영속 계약을 검증한다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JournalPrefixSelectionIntegrationTest {

    private static final String FIXTURE_JOURNAL_DATE = "2199-01-01";
    private static final String FIXTURE_CHAPTER_PREFIX = "Integration Chapter Prefix";
    private static final String FIXTURE_ENTRY_PREFIX = "Integration Entry Prefix";
    private static final String FIXTURE_OTHER_PREFIX = "Integration Other Prefix";

    @Resource
    private JournalChapterService journalChapterService;
    @Resource
    private JournalEntryService journalEntryService;
    @Resource
    private PrefixService prefixService;
    @Resource
    private PrefixContentRepository prefixContentRepository;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private JournalDayRepository journalDayRepository;
    @Resource
    private UserRepository userRepository;

    private Integer journalDayId;

    /** 테스트마다 트랜잭션 안에 독립된 저널 일자를 준비한다. */
    @BeforeEach
    void setUp() throws Exception {
        final Integer ownerId = ensureTestUser();
        JournalTestUserSupport.authenticate(ownerId, TestConstant.TEST_AUDITOR);
        final JournalDayEntity journalDay = JournalDayEntityTestFactory.createWithJournalDt(FIXTURE_JOURNAL_DATE, ownerId);
        journalDay.setYy(2199);
        journalDay.setMnth(1);
        journalDayId = journalDayRepository.saveAndFlush(journalDay).getId();
    }

    /** 개인 Prefix Scope가 참조할 가상 테스트 계정을 준비한다. */
    private Integer ensureTestUser() throws Exception {
        return JournalTestUserSupport.ensureUser(userRepository, TestConstant.TEST_AUDITOR);
    }

    /** 일반 NOTE 챕터는 JOURNAL_CHAPTER 참조에 NOTE 전용 Prefix를 연결한다. */
    @Test
    void noteChapterPersistsChapterRefWithNoteScopePrefix() throws Exception {
        saveChapter(ChapterType.DIARY, "Y");
        final PrefixDto prefix = createPrefix(ContentType.JOURNAL_CHAPTER_NOTE, FIXTURE_CHAPTER_PREFIX);
        final JournalChapterDto request = JournalChapterDto.builder()
                .journalDayId(journalDayId)
                .chapterType(ChapterType.NOTE)
                .title("Integration note chapter")
                .prefixId(prefix.getId())
                .build();

        final ServiceResponse response = journalChapterService.regist(request);
        final JournalChapterDto registered = (JournalChapterDto) response.getRsltObj();
        final PrefixContentEntity connection = requireConnection(
                registered.getId(), ContentType.JOURNAL_CHAPTER.key);

        assertEquals(prefix.getId(), connection.getPrefixId());
        assertEquals(prefix.getId(), registered.getPrefixId());
        assertEquals(prefix.getId(), registered.getPrefix().getId());
    }

    /** 첫 일반 챕터인 시스템 요약은 요청에 Prefix가 있어도 연결을 만들지 않는다. */
    @Test
    void systemSummaryChapterDoesNotPersistRequestedPrefix() throws Exception {
        final PrefixDto prefix = createPrefix(ContentType.JOURNAL_CHAPTER_DIARY, FIXTURE_CHAPTER_PREFIX);
        final JournalChapterDto request = JournalChapterDto.builder()
                .journalDayId(journalDayId)
                .chapterType(ChapterType.DIARY)
                .title("Integration summary chapter")
                .prefixId(prefix.getId())
                .build();

        final ServiceResponse response = journalChapterService.regist(request);
        final JournalChapterDto registered = (JournalChapterDto) response.getRsltObj();

        assertEquals("Y", registered.getSummaryYn());
        assertNull(registered.getPrefixId());
        assertNull(registered.getPrefix());
        assertFalse(prefixContentRepository.findByRefIdAndRefContentType(
                registered.getId(), ContentType.JOURNAL_CHAPTER.key).isPresent());
    }

    /** DIARY와 DREAM 엔트리는 영속 타입과 같은 개인 Prefix Scope를 사용한다. */
    @ParameterizedTest
    @MethodSource("entryTypeCases")
    void entryPersistsRefWithMatchingScope(
            final ChapterType chapterType,
            final ContentType contentType
    ) throws Exception {
        final Integer chapterId = saveChapter(chapterType, "N");
        final PrefixDto prefix = createPrefix(contentType, FIXTURE_ENTRY_PREFIX);
        final JournalEntryPostDto request = JournalEntryDtoTestFactory.createPost(contentType);
        request.setJournalChapterId(chapterId);
        request.setPrefixId(prefix.getId());

        final ServiceResponse response = journalEntryService.regist(contentType, request, null);
        final JournalEntryDto registered = (JournalEntryDto) response.getRsltObj();
        final PrefixContentEntity connection = requireConnection(registered.getId(), contentType.key);

        assertEquals(prefix.getId(), connection.getPrefixId());
        assertEquals(contentType.key, registered.getPrefixContentType());
    }

    /** NOTE 엔트리는 JOURNAL_DIARY 참조를 유지하면서 JOURNAL_NOTE Scope를 사용한다. */
    @Test
    void noteEntryPersistsDiaryRefWithNoteScopePrefix() throws Exception {
        final Integer chapterId = saveChapter(ChapterType.NOTE, "N");
        final PrefixDto prefix = createPrefix(ContentType.JOURNAL_NOTE, FIXTURE_ENTRY_PREFIX);
        final JournalEntryPostDto request = JournalEntryDtoTestFactory.createDiaryPost();
        request.setJournalChapterId(chapterId);
        request.setPrefixId(prefix.getId());

        final ServiceResponse response = journalEntryService.regist(ContentType.JOURNAL_DIARY, request, null);
        final JournalEntryDto registered = (JournalEntryDto) response.getRsltObj();
        final PrefixContentEntity connection = requireConnection(
                registered.getId(), ContentType.JOURNAL_DIARY.key);

        assertEquals(prefix.getId(), connection.getPrefixId());
        assertEquals(ContentType.JOURNAL_NOTE.key, registered.getPrefixContentType());
    }

    /** NOTE 엔트리는 다른 개인 Scope의 Prefix 선택을 거부한다. */
    @Test
    void noteEntryRejectsPrefixFromDiaryScope() throws Exception {
        final Integer chapterId = saveChapter(ChapterType.NOTE, "N");
        createPrefix(ContentType.JOURNAL_NOTE, FIXTURE_ENTRY_PREFIX);
        final PrefixDto diaryPrefix = createPrefix(ContentType.JOURNAL_DIARY, FIXTURE_OTHER_PREFIX);
        final JournalEntryPostDto request = JournalEntryDtoTestFactory.createDiaryPost();
        request.setJournalChapterId(chapterId);
        request.setPrefixId(diaryPrefix.getId());

        assertThrows(NotAuthorizedException.class,
                () -> journalEntryService.regist(ContentType.JOURNAL_DIARY, request, null));
    }

    private static Stream<Arguments> entryTypeCases() {
        return Stream.of(
                Arguments.of(ChapterType.DIARY, ContentType.JOURNAL_DIARY),
                Arguments.of(ChapterType.DREAM, ContentType.JOURNAL_DREAM)
        );
    }

    private Integer saveChapter(final ChapterType chapterType, final String summaryYn) {
        return journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .journalDayId(journalDayId)
                .chapterType(chapterType)
                .summaryYn(summaryYn)
                .title("Integration chapter")
                .sortOrder(1)
                .build()).getId();
    }

    private PrefixDto createPrefix(final ContentType contentType, final String name) {
        return prefixService.create(contentType.key, PrefixDto.builder()
                .name(name)
                .color("#6B7280")
                .sortOrder(1)
                .build());
    }

    private PrefixContentEntity requireConnection(final Integer refId, final String refContentType) {
        final PrefixContentEntity connection = prefixContentRepository
                .findByRefIdAndRefContentType(refId, refContentType)
                .orElse(null);
        assertNotNull(connection);
        return connection;
    }
}
