package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagService;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntrySearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryExportService {

    private final TagService tagService;

    /**
     * 엔트리 목록과 검색 조건을 텍스트 내보내기 포맷으로 변환한다.
     *
     * @param journalEntryList 내보낼 엔트리 목록
     * @param searchParam 검색 조건
     * @return 텍스트 포맷 문자열
     */
    public String buildTxt(final List<JournalEntryDto> journalEntryList, final JournalEntrySearchParam searchParam) {
        final List<TagDto> tagList = tagService.getTagListByIds(searchParam.getTagIds());

        final StringBuilder sb = new StringBuilder();
        sb.append("=== dreamdiary export ===\r\n");

        if (CollectionUtils.isNotEmpty(searchParam.getSearchKeywords())) {
            sb.append("keywords: ")
              .append(String.join(", ", searchParam.getSearchKeywords()))
              .append("\r\n");
        }

        if (CollectionUtils.isNotEmpty(tagList)) {
            final String tagLine = tagList.stream()
                    .map(tag -> "#" + tag.getTagNm())
                    .collect(Collectors.joining(", "));

            sb.append("tags: ")
              .append(tagLine)
              .append("\r\n");
        }

        sb.append("total: ")
          .append(CollectionUtils.isEmpty(journalEntryList) ? 0 : journalEntryList.size())
          .append("\r\n");
        sb.append("================================\r\n\n");
        if (CollectionUtils.isEmpty(journalEntryList)) return sb.toString();

        String prevDate = null;
        for (final JournalEntryDto entry : journalEntryList) {
            final String stdrdDt = entry.getStdrdDt();
            final String journalWeekDay = StringUtils.isEmpty(entry.getJournalDateWeekDay()) ? "" : "(" + entry.getJournalDateWeekDay() + ")";
            final String date = stdrdDt + journalWeekDay;

            if (!Objects.equals(prevDate, date)) {
                sb.append("\r\n").append(date).append("\r\n");
                prevDate = date;
            }

            sb.append("#")
              .append(entry.getSortOrder())
              .append("\r\n")
              .append(CmmUtils.htmlToText(entry.getContent()))
              .append("\r\n");

            if (entry.getTag() == null || CollectionUtils.isEmpty(entry.getTag().getList())) {
                sb.append("\r\n\n");
                continue;
            }

            final List<TagContentDto> tagDtoList = entry.getTag().getList();
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
