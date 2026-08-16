package io.nicheblog.dreamdiary.feature.journal.chapter.service;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagService;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
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

    /**
     * 챕터 DTO를 텍스트 내보내기 포맷으로 변환한다.
     *
     * @param entry 챕터 DTO
     * @param includeReflection 해석 포함 여부 (기본 true). 포함 시 각 엔트리를 target 으로 한 리플렉션 본문을 이어 붙인다.
     * @return 텍스트 포맷 문자열
     */
    public String buildTxt(final JournalChapterDto entry, final boolean includeReflection) {
        if (entry == null) return "";

        final StringBuilder sb = new StringBuilder();
        // 기본 헤더 정보를 작성한다.
        sb.append("=== dreamdiary export ===\r\n");

        final String stdrdDt = entry.getStdrdDt();
        final String journalWeekDay = StringUtils.isEmpty(entry.getJournalDateWeekDay()) ? "" : "(" + entry.getJournalDateWeekDay() + ")";
        final String date = stdrdDt + journalWeekDay;
        sb.append("\r\n").append(date).append("\r\n");

        final List<JournalEntryDto> journalDiaryList = JournalEntryViewProjectionHelper.getDiaryEntries(entry);

        final int diaryCnt = CollectionUtils.isEmpty(journalDiaryList) ? 0 : journalDiaryList.size();
        sb.append("diaries: ").append(diaryCnt).append("\r\n");
        sb.append("================================\r\n\n");

        if (CollectionUtils.isNotEmpty(journalDiaryList)) {
            sb.append("[diaries]\r\n");
            for (final JournalEntryDto diary : journalDiaryList) {
                sb.append("#")
                  .append(diary.getSortOrder())
                  .append("\r\n")
                  .append(CmmUtils.htmlToText(diary.getContent()))
                  .append("\r\n");
                if (includeReflection && CollectionUtils.isNotEmpty(diary.getReflectionList())) {
                    for (final JournalEntryDto reflection : diary.getReflectionList()) {
                        if (reflection == null) continue;
                        final String reflText = CmmUtils.htmlToText(reflection.getContent());
                        if (StringUtils.isEmpty(reflText)) continue;
                        sb.append("\r\n").append(reflText).append("\r\n");
                    }
                }
                if (diary.getTag() == null || CollectionUtils.isEmpty(diary.getTag().getList())) {
                    sb.append("\r\n\n");
                    continue;
                }
                final List<TagContentDto> tagDtoList = diary.getTag().getList();
                for (final TagContentDto tagDto : tagDtoList) {
                    final String ctgr = StringUtils.isEmpty(tagDto.getCtgr()) ? "" : "[" + tagDto.getCtgr() + "] ";
                    final String tagStr = ctgr + tagDto.getName();
                    sb.append("#")
                      .append(tagStr)
                      .append(" ");
                }
                sb.append("\r\n\n");
            }
        }

        return sb.toString();
    }
}
