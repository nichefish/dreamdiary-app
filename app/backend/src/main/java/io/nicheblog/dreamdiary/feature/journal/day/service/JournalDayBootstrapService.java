package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.JournalChapterService;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalDayBootstrapService
 * <pre>
 *  저널 일자 생성 직후 필요한 기본 하위 구조를 보장한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDayBootstrapService {

    private final JournalDayRepository journalDayRepository;
    private final JournalChapterRepository journalChapterRepository;
    private final ObjectProvider<JournalChapterService> journalChapterServiceProvider;

    /**
     * 저널 일자에 기본 SUMMARY 챕터와 빈 DIARY 엔트리 구조를 보장한다.
     * 기존 DIARY 챕터가 이미 있으면 아무 작업도 하지 않는다.
     *
     * @param journalDayId 저널 일자 ID
     */
    @Transactional
    public void ensureDefaultSummaryDiary(final Integer journalDayId) throws Exception {
        if (journalDayId == null) {
            log.warn("Journal day bootstrap skipped. journalDayId is null.");
            throw new BusinessException("msg.journal.day.not-found");
        }

        final JournalDayEntity day = journalDayRepository.findById(journalDayId)
                .orElseThrow(() -> new BusinessException("msg.journal.day.not-found"));
        if (!AuthUtils.isCreatedBy(day.getCreatedBy())) {
            log.warn("Journal day bootstrap ownership check failed. journalDayId={}, createdBy={}, loginUsername={}",
                    journalDayId, day.getCreatedBy(), AuthUtils.getLoginUsername());
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        if (journalChapterRepository.findFirstByJournalDayIdAndChapterType(journalDayId, ChapterType.DIARY).isPresent()) {
            log.debug("Journal day bootstrap skipped. DIARY chapter already exists. journalDayId={}", journalDayId);
            return;
        }

        log.info("Journal day bootstrap creating default SUMMARY DIARY. journalDayId={}, createdBy={}",
                journalDayId, day.getCreatedBy());
        final JournalChapterDto chapterDto = new JournalChapterDto();
        chapterDto.setJournalDayId(journalDayId);
        chapterDto.setChapterType(ChapterType.DIARY);
        journalChapterServiceProvider.getObject().regist(chapterDto);
    }
}
