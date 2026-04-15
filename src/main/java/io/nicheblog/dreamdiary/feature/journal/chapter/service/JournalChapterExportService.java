package io.nicheblog.dreamdiary.feature.journal.chapter.service;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JournalChapterExportService
 * <pre>
 *  저널 챕터 내보내기 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalChapterExportService {

    private final TagService tagService;

    public String buildTxt(final JournalChapterDto entry) {
        if (entry == null) return "";

        final StringBuilder sb = new StringBuilder();
        // =========================
        // 1. 검색 조건 헤더
        // =========================
        sb.append("=== dreamdiary export ===\r\n");

        final String stdrdDt = entry.getStdrdDt();
        final String journalWeekDay = StringUtils.isEmpty(entry.getJournalDtWeekDay()) ? "" : "(" + entry.getJournalDtWeekDay() + ")";
        final String date = stdrdDt + journalWeekDay;
        sb.append("\r\n").append(date).append("\r\n");

        final List<JournalDiaryDto> journalDiaryList = entry.getJournalDiaryList();

        sb.append("total: ")
          .append(CollectionUtils.isEmpty(journalDiaryList) ? 0 : journalDiaryList.size())
          .append("\r\n");
        sb.append("================================\r\n\n");
        if (CollectionUtils.isEmpty(journalDiaryList)) return sb.toString();

        // =========================
        // 2. 본문
        // =========================
        for (final JournalDiaryDto diary : journalDiaryList) {
            sb.append("#")
              .append(diary.getSortOrder())
              .append("\r\n")
              .append(CmmUtils.htmlToText(diary.getContent()))
              .append("\r\n");
            // 태그 처리
            if (diary.getTag() == null || CollectionUtils.isEmpty(diary.getTag().getList())) {
                sb.append("\r\n\n");
                continue;
            }
            final List<TagContentDto> tagDtoList = diary.getTag().getList();
            for (final TagContentDto tagDto : tagDtoList) {
                final String ctgr = StringUtils.isEmpty(tagDto.getCtgr()) ? "" : "[" + tagDto.getCtgr() + "] ";
                final String tagStr = ctgr + tagDto.getTagNm();
                sb.append("#")
                  .append(tagStr)
                  .append(" ");
            }
            sb.append("\r\n\n");
        }

        return sb.toString();
    }
}


