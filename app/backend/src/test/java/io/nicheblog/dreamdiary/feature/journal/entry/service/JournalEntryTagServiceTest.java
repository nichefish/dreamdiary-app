package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagProfileService;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryTagRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.spec.JournalEntryTagSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 저널 엔트리 태그 카테고리 맵의 projection 조회 계약을 검증한다. */
@ExtendWith(MockitoExtension.class)
class JournalEntryTagServiceTest {

    private static final String FIXTURE_USERNAME = "fixture-user";

    @Mock
    private JournalEntryTagRepository repository;
    @Mock
    private JournalEntryTagSpec spec;
    @Mock
    private TagProfileService tagProfileService;

    @InjectMocks
    private JournalEntryTagService service;

    /** 일기 카테고리 맵은 일기 단일 축 projection 행을 태그명별 카테고리 목록으로 묶는다. */
    @Test
    void getTagCategoryMapByUserGroupsDiaryProjectionRows() throws Exception {
        final List<String> diaryAxis = List.of(ContentType.JOURNAL_DIARY.key);
        when(repository.findCategoryRowsByUserAndContentTypes(FIXTURE_USERNAME, diaryAxis)).thenReturn(List.of(
                new TagDto(1, "alpha", "category-a"),
                new TagDto(2, "alpha", "category-b"),
                new TagDto(3, "beta", "")
        ));

        final Map<String, List<String>> result = service.getTagCategoryMapByUser(
                FIXTURE_USERNAME,
                ContentType.JOURNAL_DIARY
        );

        assertEquals(List.of("category-a", "category-b"), result.get("alpha"));
        assertEquals(List.of(""), result.get("beta"));
        verify(repository).findCategoryRowsByUserAndContentTypes(FIXTURE_USERNAME, diaryAxis);
    }
}
