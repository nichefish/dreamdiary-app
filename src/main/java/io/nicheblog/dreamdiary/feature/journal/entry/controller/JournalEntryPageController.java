package io.nicheblog.dreamdiary.feature.journal.entry.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.entry.type.JournalEntryType;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class JournalEntryPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_EMTRY_SEARCH;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;

    /**
     * 엔트리 검색 페이지를 타입별로 렌더링한다.
     *
     * @param type 엔트리 타입 경로값
     * @param model 뷰 모델
     * @return 뷰 경로
     */
    @GetMapping(Url.JOURNAL_EMTRY_SEARCH)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalEntrySearch(
            final @PathVariable String type,
            final ModelMap model
    ) {
        final JournalEntryType contentType = JournalEntryType.from(type);
        model.addAttribute("journalEntrySearchContentType", contentType.getContentType());
        return "/view/feature/journal/entry/journal_entry_search";
    }

    /**
     * 저널 일기/꿈 작성 중 미리보기 팝업.
     *
     * @param journalEntry 작성 중 폼 데이터
     * @param type 일기/꿈 구분 (diary, dream, JOURNAL_DIARY 등 {@link JournalEntryType} alias)
     * @param model 뷰 모델
     * @return 미리보기 뷰 경로
     */
    @PostMapping(Url.JOURNAL_ENTRY_PREVIEW_POP)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalEntryPreviewPop(
            final JournalEntryPostDto journalEntry,
            @RequestParam final String type,
            final ModelMap model
    ) {

        model.addAttribute("menuLabel", SiteMenu.JOURNAL_DAY);
        model.addAttribute("pageName", PageName.PREVIEW);

        final JournalEntryType entryType = JournalEntryType.from(type);
        model.addAttribute("entry", journalEntry);
        model.addAttribute("previewMarkdownContent", MarkdownUtils.markdown(journalEntry.getContent()));
        model.addAttribute("previewContentType", entryType.getContentType());

        return "/view/feature/journal/entry/journal_entry_preview_pop";
    }
}
