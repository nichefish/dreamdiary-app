package io.nicheblog.dreamdiary.feature.journal.diary.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.journal.diary.service.JournalDiaryService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * JournalDiaryRestController
 * <pre>
 *  저널 일기 RestController.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
public class JournalDiaryPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DIARY_SEARCH;         // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final JournalDiaryService journalDiaryService;

    /**
     * 저널 일기 (검색) 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JOURNAL_DIARY_SEARCH)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalDiarySearch(
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JOURNAL_DAY);
        model.addAttribute("pageNm", PageNm.LIST);

        return "/view/feature/journal/diary/journal_diary_search";
    }
}

