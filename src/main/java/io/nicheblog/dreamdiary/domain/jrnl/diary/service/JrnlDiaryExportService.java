package io.nicheblog.dreamdiary.domain.jrnl.diary.service;

import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.domain.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.domain.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.domain.clsf.tag.service.TagService;
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
 * JrnlDiaryExportService
 * <pre>
 *  저널 일기 내보내기 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDiaryExportService")
@RequiredArgsConstructor
@Log4j2
public class JrnlDiaryExportService {

    private final TagService tagService;

    /**
     * 저널 일기 txt 다룬로드
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlDiaryList List<JrnlDiaryDto>
     * @param searchParam JrnlDiarySearchParam
     * @return 내보내기 txt
     */
    public String buildTxt(final List<JrnlDiaryDto> jrnlDiaryList, final JrnlDiarySearchParam searchParam) {

        final List<TagDto> tagList = tagService.getTagListByTagNos(searchParam.getTagNos());

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
          .append(CollectionUtils.isEmpty(jrnlDiaryList) ? 0 : jrnlDiaryList.size())
          .append("\r\n");
        sb.append("================================\r\n\n");
        if (CollectionUtils.isEmpty(jrnlDiaryList)) return sb.toString();

        // =========================
        // 2. 본문
        // =========================
        String prevDate = null;
        for (final JrnlDiaryDto diary: jrnlDiaryList) {
            final String stdrdDt = diary.getStdrdDt();
            final String jrnlWeekDay = StringUtils.isEmpty(diary.getJrnlDtWeekDay()) ? "" : "(" + diary.getJrnlDtWeekDay() + ")";
            final String date = stdrdDt + jrnlWeekDay;

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
