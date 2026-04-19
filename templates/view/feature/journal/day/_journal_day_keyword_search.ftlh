<#--
 * journal_day: keyword search (popup)
 * @included_in : journal_day_*.ftlh
 -->

<div id="journal_day_keyword_search" class="mt-3">
    <div class="flex-grow-1 d-flex flex-wrap gap-3">
        <div class="d-flex align-items-center gap-2">
            <input type="text" name="diarySearchKeyword" id="diarySearchKeyword" class="form-control form-control-sm"
                   value="" placeholder="<@spring.message 'txt.journal.diary.keyword.search'/>" maxlength="200"/>
            <button type="button" id="journal_diary_search_btn" class="btn btn-sm btn-outline btn-light-primary px-3"
                    onclick="dF.JournalDiary.searchPopup();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.txt.journal.diary.keyword.search'/>">
                <i class="bi bi-search pe-0"></i>
            </button>
        </div>
        <div class="d-flex align-items-center gap-2">
            <input type="text" name="dreamSearchKeyword" id="dreamSearchKeyword" class="form-control form-control-sm"
                   value="" placeholder="<@spring.message 'txt.journal.dream.keyword.search'/>" maxlength="200"/>
            <button type="button" id="journal_dream_search_btn" class="btn btn-sm btn-outline btn-light-primary px-3"
                    onclick="dF.JournalDream.searchPopup();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.txt.journal.dream.keyword.search'/>">
                <i class="bi bi-search pe-0"></i>
            </button>
        </div>
    </div>
</div>
