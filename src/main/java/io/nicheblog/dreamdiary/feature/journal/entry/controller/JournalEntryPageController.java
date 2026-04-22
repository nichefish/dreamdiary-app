package io.nicheblog.dreamdiary.feature.journal.entry.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.journal.entry.type.JournalEntryType;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
