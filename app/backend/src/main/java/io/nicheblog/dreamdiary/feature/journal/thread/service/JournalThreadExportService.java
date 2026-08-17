package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
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
 * JournalThreadExportService
 * <pre>
 *  저널 스레드 내보내기 서비스 모듈.
 *  스레드 제목을 머리행으로, 소속 엔트리를 일자별로 이어붙여 텍스트로 변환한다.
 *  포맷은 챕터/엔트리 내보내기(JournalEntryExportService)와 동일한 배너·엔트리 블록 계약을 따른다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalThreadExportService {

    /**
     * 스레드 DTO와 소속 엔트리 목록을 텍스트 내보내기 포맷으로 변환한다.
     * <p>
     * 소속 엔트리는 {@link io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadEntryService#getEntriesByThread}
     * 가 일자 오름차순으로 내려준 순서를 그대로 사용한다. 태그·요일이 채워지지 않은 경량 엔트리도 안전하게 처리한다.
     *
     * @param thread 스레드 DTO (제목·말머리)
     * @param entries 소속 엔트리 목록 (일자 오름차순)
     * @param includeReflection 해석 포함 여부 (기본 true). 포함 시 각 엔트리를 target 으로 한 리플렉션 본문을 이어 붙인다.
     * @return 텍스트 포맷 문자열
     */
    public String buildTxt(final JournalThreadDto thread, final List<JournalEntryDto> entries, final boolean includeReflection) {
        final StringBuilder sb = new StringBuilder();
        sb.append("=== dreamdiary export ===\r\n");

        final String title = (thread == null || StringUtils.isEmpty(thread.getTitle())) ? "" : thread.getTitle();
        sb.append("thread: ").append(title).append("\r\n");
        final String prefixName = prefixName(thread);
        if (!StringUtils.isEmpty(prefixName)) {
            sb.append("prefix: ").append(prefixName).append("\r\n");
        }
        sb.append("total: ")
          .append(CollectionUtils.isEmpty(entries) ? 0 : entries.size())
          .append("\r\n");
        sb.append("================================\r\n\n");
        if (CollectionUtils.isEmpty(entries)) return sb.toString();

        String prevDate = null;
        for (final JournalEntryDto entry : entries) {
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

            if (includeReflection && CollectionUtils.isNotEmpty(entry.getReflectionList())) {
                for (final JournalEntryDto reflection : entry.getReflectionList()) {
                    if (reflection == null) continue;
                    final String reflText = CmmUtils.htmlToText(reflection.getContent());
                    if (StringUtils.isEmpty(reflText)) continue;
                    sb.append("\r\n").append(reflText).append("\r\n");
                }
            }

            if (entry.getTag() == null || CollectionUtils.isEmpty(entry.getTag().getList())) {
                sb.append("\r\n\n");
                continue;
            }
            final List<TagContentDto> tagDtoList = entry.getTag().getList();
            for (final TagContentDto tagDto : tagDtoList) {
                final String ctgr = StringUtils.isEmpty(tagDto.getCtgr()) ? "" : "[" + tagDto.getCtgr() + "] ";
                final String tagStr = ctgr + tagDto.getName();
                sb.append("#").append(tagStr).append(" ");
            }
            sb.append("\r\n\n");
        }
        return sb.toString();
    }

    /** 선택된 단일 말머리 이름을 내보내기 표시 문자열로 만든다. */
    private String prefixName(final JournalThreadDto thread) {
        if (thread == null || thread.getPrefix() == null) return "";
        return thread.getPrefix().getName() == null ? "" : thread.getPrefix().getName();
    }
}
