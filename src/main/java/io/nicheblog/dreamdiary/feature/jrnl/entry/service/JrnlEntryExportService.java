package io.nicheblog.dreamdiary.feature.jrnl.entry.service;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JrnlEntryExportService
 * <pre>
 *  저널 항목 내보내기 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlEntryExportService")
@RequiredArgsConstructor
@Log4j2
public class JrnlEntryExportService {

    private final TagService tagService;

    public String buildTxt(final JrnlEntryDto entry) {
        if (entry == null) return "";

        final StringBuilder sb = new StringBuilder();
        // =========================
        // 1. 검색 조건 헤더
        // =========================
        sb.append("=== dreamdiary export ===\r\n");

        final String stdrdDt = entry.getStdrdDt();
        final String jrnlWeekDay = StringUtils.isEmpty(entry.getJrnlDtWeekDay()) ? "" : "(" + entry.getJrnlDtWeekDay() + ")";
        final String date = stdrdDt + jrnlWeekDay;
        sb.append("\r\n").append(date).append("\r\n");

        final List<JrnlDiaryDto> jrnlDiaryList = entry.getJrnlDiaryList();

        sb.append("total: ")
          .append(CollectionUtils.isEmpty(jrnlDiaryList) ? 0 : jrnlDiaryList.size())
          .append("\r\n");
        sb.append("================================\r\n\n");
        if (CollectionUtils.isEmpty(jrnlDiaryList)) return sb.toString();

        // =========================
        // 2. 본문
        // =========================
        for (final JrnlDiaryDto diary : jrnlDiaryList) {
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
