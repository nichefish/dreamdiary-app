package io.nicheblog.dreamdiary.feature.journal.diary.service;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiarySearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JournalDiaryExportService
 * <pre>
 *  저널 일기 내보내기 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDiaryExportService {

    private final TagService tagService;

    /**
     * 저널 일기 txt 다룬로드
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param journalDiaryList 저널 일기 목록
     * @param searchParam JournalDiarySearchParam
     * @return 내보내기 txt
     */
    public String buildTxt(final List<JournalDiaryDto> journalDiaryList, final JournalDiarySearchParam searchParam) {

        final List<TagDto> tagList = tagService.getTagListByIds(searchParam.getTagIds());

        final StringBuilder sb = new StringBuilder();
        // =========================
        // 1. 검색 조건 헤더
        // =========================
        sb.append("=== dreamdiary export ===\r\n");
        // search keywords
        if (CollectionUtils.isNotEmpty(searchParam.getSearchKeywords())) {
            sb.append("keywords: ")
              .append(String.join(", ", searchParam.getSearchKeywords()))
              .append("\r\n");
        }
        // search tags
        if (CollectionUtils.isNotEmpty(tagList)) {
            final String tagLine = tagList.stream()
                    .map(tag -> "#" + tag.getTagNm())
                    .collect(Collectors.joining(", "));

            sb.append("tags: ")
              .append(tagLine)
              .append("\r\n");
        }
        sb.append("total: ")
          .append(CollectionUtils.isEmpty(journalDiaryList) ? 0 : journalDiaryList.size())
          .append("\r\n");
        sb.append("================================\r\n\n");
        if (CollectionUtils.isEmpty(journalDiaryList)) return sb.toString();

        // =========================
        // 2. 본문
        // =========================
        String prevDate = null;
        for (final JournalDiaryDto diary: journalDiaryList) {
            final String stdrdDt = diary.getStdrdDt();
            final String journalWeekDay = StringUtils.isEmpty(diary.getJournalDtWeekDay()) ? "" : "(" + diary.getJournalDtWeekDay() + ")";
            final String date = stdrdDt + journalWeekDay;

            if (!Objects.equals(prevDate, date)) {
                sb.append("\r\n").append(date).append("\r\n");
                prevDate = date;
            }

            sb.append("#")
              .append(diary.getIdx())
              .append("\r\n")
              .append(CmmUtils.htmlToText(diary.getCn()))
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


