<#--
 * 저널 일기/꿈(journal_entry) > 작성 중 미리보기 팝업
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />
<link rel="stylesheet" type="text/css" href="/css/markdown.css?${releaseDate!}" />

<div class="card post">
    <div class="card-body">
        <div class="mb-0">
            <div class="d-flex flex-wrap align-items-baseline gap-3 mb-2">
                <span class="fs-3 fw-bold text-gray-900">
                    <#if previewContentType! == "JOURNAL_DREAM">
                        <i class="bi bi-moon-stars me-2 text-primary"></i><@spring.message 'txt.journal.dream'/>
                    <#else>
                        <i class="bi bi-journal-text me-2 text-primary"></i><@spring.message 'txt.journal.diary'/>
                    </#if>
                </span>
                <#if entry.sortOrder??>
                    <span class="text-muted fs-6"># ${entry.sortOrder}</span>
                </#if>
            </div>
            <#if entry.title?? && entry.title?has_content>
                <div class="fs-5 fw-semibold text-gray-800 mb-2">${entry.title}</div>
            </#if>
        </div>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <#assign previewEntryContentClass = "journal-diary-content">
        <#assign previewEntryItemClass = "journal-diary-item">
        <#if previewContentType! == "JOURNAL_DREAM">
            <#assign previewEntryContentClass = "journal-dream-content">
            <#assign previewEntryItemClass = "journal-dream-item">
        </#if>

        <div class="px-2 py-1 pb-4 journal-markdown-preview">
            <div class="${previewEntryItemClass}">
                <div class="col">
                    <div class="${previewEntryContentClass}">
                        <div class="journal-content">
                            ${previewMarkdownContent?no_esc}
                        </div>
                    </div>
                </div>
                <div class="col-1 py-3 d-none d-md-flex w-25px ps-2">&nbsp;</div>
            </div>
        </div>
    </div>
    <div class="card-footer">
        <div class="d-flex justify-content-end">
            <@component.popup_btn_preview_close "Layout" />
        </div>
    </div>
</div>

</@layout.layout>
