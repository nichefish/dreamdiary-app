/**
 * jrnl_day_tag_module.ts
 * 저널 일자 태그 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDayTag = (function(): dfModule {
    return {
        initialized: false,
        ctgrMap: new Map(),
        contextMenuEl: null,
        contextMenuState: null,
        contextMenuBound: false,
        contextAnchorEl: null,
        contextAnchorPrevStyle: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.JrnlDayTag.initialized) return;

            dF.JrnlDayTag.getCtgrMap();
            dF.JrnlDayTag.bindContextMenuEvents();
            dF.JrnlDayTag.syncContextTooltipTargets();

            dF.JrnlDayTag.initialized = true;
            console.log("'dF.JrnlDayTag' module initialized.");
        },

        /**
         * 태그 카테고리 맵 조회
         */
        getCtgrMap: function(): void {
            const url: string = Url.JRNL_DAY_TAG_CTGR_MAP;
            cF.ajax.get(url, {}, function(res: AjaxResponse): void {
                if (res.rsltMap) dF.JrnlDayTag.ctgrMap = res.rsltMap;
            });
        },

        /**
         * 연도 선택 처리
         * @param {string|number} yy
         */
        getSelectedYy: function(yy?: string|number): string {
            if (yy != null && cF.util.isNotEmpty(String(yy))) return String(yy);

            const currentSearchYy: string = dF.JrnlDay?.currentSearchParams?.yy;
            if (cF.util.isNotEmpty(currentSearchYy)) return currentSearchYy;

            const urlYy: string = cF.util.getUrlParam("yy");
            if (cF.util.isNotEmpty(urlYy)) return urlYy;

            return cF.date.getCurrYyStr();
        },

        getCurrentWeekStartDt: function(): string {
            const currentWeekStartDt: string = dF.JrnlDay?.currentSearchParams?.weekStartDt;
            if (cF.util.isNotEmpty(currentWeekStartDt)) return currentWeekStartDt;

            if (dF.JrnlDay?.viewType === "WEEKLY" && cF.util.isNotEmpty(Page?.weekStartDt)) return Page.weekStartDt;

            const stdrdDt: string = dF.JrnlDay?.currentSearchParams?.stdrdDt
                ?? Page?.stdrdDt
                ?? cF.date.getCurrDateStr(cF.date.ptnDate);
            return cF.date.getWeekdayDateStr(stdrdDt, 1, cF.date.ptnDate) ?? stdrdDt;
        },

        /**
         * 선택 연도 정합성 처리
         * @param {string} selectedYy
         * @param {(string|number)[]} yyList
         */
        normalizeSelectedYy: function(selectedYy: string, yyList: (string|number)[]): string {
            if (yyList.length === 0) return selectedYy;

            const matchedYy = yyList.find((yy: string|number): boolean => String(yy) === String(selectedYy));
            if (matchedYy != null) return String(matchedYy);

            return String(yyList[0]);
        },

        /**
         * 연도 옵션 처리
         * @param {string} selectedYy
         * @param {(string|number)[]} yyList
         */
        getYearOptions: function(selectedYy: string, yyList: (string|number)[]): Record<string, any>[] {
            return yyList.map((yy: string|number): Record<string, any> => ({
                value: yy,
                label: yy,
                selected: String(yy) === String(selectedYy),
            }));
        },

        getYyListAjax: function(tagId: string|number, callback: (yyList: any[]) => void): void {
            const url: string = cF.util.bindUrl(Url.JRNL_DAY_TAG_YYS, { tagId });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                callback(Array.isArray(res.rsltList) ? res.rsltList : []);
            });
        },

        /**
         * 목록에 따른 일자 태그 조회 (Ajax)
         */
        listAjax: function(): void {
            const url: string = Url.JRNL_DAY_TAGS;
            const ajaxData: Record<string, any> = {};
            if (dF.JrnlDay?.viewType === "WEEKLY") {
                const weekStartDt: string = dF.JrnlDayTag.getCurrentWeekStartDt();
                if (cF.util.isEmpty(weekStartDt)) return;
                ajaxData.weekStartDt = weekStartDt;
            } else {
                const yy: string = cF.util.getUrlParam("yy") ?? localStorage.getItem("jrnl_yy") ?? "9999";
                if (cF.util.isEmpty(yy)) return;
                const mnth: string = cF.util.getUrlParam("mnth") ?? localStorage.getItem("jrnl_mnth") ?? "99";
                if (cF.util.isEmpty(mnth)) return;
                ajaxData.yy = yy;
                ajaxData.mnth = mnth;
            }
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                cF.handlebars.template(res.rsltList, "jrnl_day_tag_list");
            });
        },

        /**
         * 목록에 따른 일자 태그 (전체) 조회 (Ajax)
         */
        listAllAjax: function(): void {
            const url: string = Url.JRNL_DAY_TAGS;
            const ajaxData: Record<string, any> = { "yy": 9999, "mnth":99 };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                // 상단에 태그 카테고리 메뉴 생성
                const ctgrSet: Set<string> = new Set();
                res.rsltList.forEach((item: Record<string, string>): void => {
                    if (item.ctgr) ctgrSet.add(item.ctgr);
                });
                cF.handlebars.template(ctgrSet, "jrnl_tag_ctgr");
                cF.handlebars.modal(res.rsltList, "jrnl_tag_list");
                $("#jrnl_tag_dtl_modal").modal("hide");
            });
        },

        isContextMenuEnabled: function(): boolean {
            return dF.JrnlDay?.initialized === true;
        },

        getCurrentClickEvent: function(): Event | null {
            return (typeof event !== "undefined" && event) ? event as Event : null;
        },

        stopCurrentClickEvent: function(): void {
            const currentEvent: Event | null = dF.JrnlDayTag.getCurrentClickEvent();
            if (!currentEvent) return;

            currentEvent.stopPropagation();
            if (typeof currentEvent.preventDefault === "function") currentEvent.preventDefault();
        },

        getContextAnchorEl: function(): HTMLElement | null {
            const currentEvent: Event | null = dF.JrnlDayTag.getCurrentClickEvent();
            if (!(currentEvent?.target instanceof HTMLElement)) return null;

            return currentEvent.target.closest("[onclick]") as HTMLElement | null;
        },

        highlightContextAnchor: function(anchorEl?: HTMLElement | null): void {
            dF.JrnlDayTag.resetContextAnchor();

            if (!(anchorEl instanceof HTMLElement)) return;

            dF.JrnlDayTag.contextAnchorEl = anchorEl;
            dF.JrnlDayTag.contextAnchorPrevStyle = anchorEl.getAttribute("style");
            anchorEl.style.background = "#eef6ff";
            anchorEl.style.color = "#0b63ce";
            anchorEl.style.borderRadius = "10px";
            anchorEl.style.boxShadow = "0 0 0 1px rgba(11, 99, 206, 0.18), 0 10px 24px rgba(11, 99, 206, 0.12)";
            anchorEl.style.transition = "background-color 0.18s ease, color 0.18s ease, box-shadow 0.18s ease";
        },

        resetContextAnchor: function(): void {
            const anchorEl: HTMLElement | null = dF.JrnlDayTag.contextAnchorEl;
            if (!(anchorEl instanceof HTMLElement)) return;

            const prevStyle: string | null = dF.JrnlDayTag.contextAnchorPrevStyle;
            if (prevStyle == null || prevStyle === "") anchorEl.removeAttribute("style");
            else anchorEl.setAttribute("style", prevStyle);

            dF.JrnlDayTag.contextAnchorEl = null;
            dF.JrnlDayTag.contextAnchorPrevStyle = null;
        },

        getContextTooltipSelector: function(): string {
            return [
                "[onclick*='dF.JrnlDayTag.select(']",
                "[onclick*='dF.JrnlDiaryTag.select(']",
                "[onclick*='dF.JrnlDreamTag.select(']",
                "[onclick*='dF.JrnlIntrptTag.select(']",
            ].join(", ");
        },

        applyTooltipText: function(targetEl: HTMLElement, messageKey: string): void {
            const message: string = Message.get(messageKey);
            targetEl.setAttribute("title", message);
            targetEl.setAttribute("data-bs-original-title", message);
            targetEl.setAttribute("aria-label", message);
        },

        syncContextTooltipTargets: function(rootEl?: ParentNode): void {
            if (!dF.JrnlDayTag.isContextMenuEnabled()) return;

            const queryRoot: ParentNode = rootEl ?? document;
            queryRoot.querySelectorAll(dF.JrnlDayTag.getContextTooltipSelector()).forEach(function(el: Element): void {
                dF.JrnlDayTag.applyTooltipText(el as HTMLElement, "bs.tooltip.context.menu.show");
            });
        },

        ensureContextMenu: function(): HTMLElement {
            if (dF.JrnlDayTag.contextMenuEl instanceof HTMLElement) return dF.JrnlDayTag.contextMenuEl;

            const menuEl: HTMLDivElement = document.createElement("div");
            menuEl.id = "jrnl_day_tag_context_menu";
            menuEl.style.cssText = [
                "position: fixed",
                "display: none",
                "min-width: 176px",
                "padding: 8px",
                "background: rgba(255, 255, 255, 0.96)",
                "backdrop-filter: blur(10px)",
                "border: 1px solid rgba(11, 99, 206, 0.12)",
                "border-radius: 14px",
                "box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18)",
                "z-index: 2000",
            ].join("; ");
            menuEl.innerHTML = [
                "<button type='button' data-action='search' style='display: flex; align-items: center; gap: 10px; width: 100%; padding: 10px 12px; border: 0; background: #edf5ff; color: #0b63ce; text-align: left; border-radius: 10px; font-weight: 600;'>",
                "<i class='bi bi-search' style='font-size: 13px;'></i>",
                "<span>\uAC80\uC0C9</span>",
                "</button>",
                "<button type='button' data-action='configure' style='display: flex; align-items: center; gap: 10px; width: 100%; padding: 10px 12px; border: 0; background: transparent; color: #7c2d12; text-align: left; border-radius: 10px; cursor: pointer; margin-top: 4px;'>",
                "<i class='bi bi-sliders2' style='font-size: 13px;'></i>",
                "<span>\uD0DC\uADF8 \uC124\uC815</span>",
                "</button>",
            ].join("");
            dF.JrnlDayTag.applyTooltipText(menuEl.querySelector("[data-action='search']") as HTMLElement, "view.tag.content-list");
            menuEl.addEventListener("click", function(evt: Event): void {
                evt.stopPropagation();

                let target: HTMLElement | null = evt.target as HTMLElement | null;
                while (target && target !== menuEl && !target.hasAttribute("data-action")) {
                    target = target.parentElement;
                }

                if (!target || target === menuEl) return;
                if (target.getAttribute("data-action") === "search") dF.JrnlDayTag.runContextMenuSearch();
                if (target.getAttribute("data-action") === "configure") dF.JrnlDayTag.runContextMenuConfigure();
            });
            menuEl.querySelectorAll("button[data-action]").forEach(function(button: Element): void {
                const btn: HTMLElement = button as HTMLElement;
                const baseBg: string = btn.getAttribute("data-action") === "search" ? "#edf5ff" : "transparent";
                const hoverBg: string = btn.getAttribute("data-action") === "search" ? "#dbeafe" : "#ffedd5";
                btn.addEventListener("mouseenter", function(): void {
                    btn.style.background = hoverBg;
                });
                btn.addEventListener("mouseleave", function(): void {
                    btn.style.background = baseBg;
                });
            });

            document.body.appendChild(menuEl);
            dF.JrnlDayTag.contextMenuEl = menuEl;
            return menuEl;
        },

        bindContextMenuEvents: function(): void {
            if (dF.JrnlDayTag.contextMenuBound) return;

            document.addEventListener("click", function(evt: Event): void {
                const menuEl: HTMLElement | null = dF.JrnlDayTag.contextMenuEl;
                if (!(menuEl instanceof HTMLElement)) return;
                if (menuEl.contains(evt.target as Node)) return;

                dF.JrnlDayTag.hideContextMenu();
            });

            document.addEventListener("mouseover", function(evt: Event): void {
                if (!dF.JrnlDayTag.isContextMenuEnabled()) return;
                if (!(evt.target instanceof HTMLElement)) return;

                const tooltipTarget: HTMLElement | null = evt.target.closest(dF.JrnlDayTag.getContextTooltipSelector()) as HTMLElement | null;
                if (!tooltipTarget) return;

                dF.JrnlDayTag.applyTooltipText(tooltipTarget, "bs.tooltip.context.menu.show");
            });

            document.addEventListener("keydown", function(evt: KeyboardEvent): void {
                if (evt.key === "Escape") dF.JrnlDayTag.hideContextMenu();
            });

            window.addEventListener("resize", function(): void {
                dF.JrnlDayTag.hideContextMenu();
            });

            window.addEventListener("scroll", function(): void {
                dF.JrnlDayTag.hideContextMenu();
            }, true);

            dF.JrnlDayTag.contextMenuBound = true;
        },

        getContextMenuPosition: function(menuEl: HTMLElement): Record<string, number> {
            const currentEvent: Event | null = dF.JrnlDayTag.getCurrentClickEvent();
            let left: number = 16;
            let top: number = 16;

            if (currentEvent instanceof MouseEvent) {
                left = currentEvent.clientX;
                top = currentEvent.clientY + 8;
            } else if (currentEvent?.target instanceof HTMLElement) {
                const rect: DOMRect = currentEvent.target.getBoundingClientRect();
                left = rect.left;
                top = rect.bottom + 8;
            }

            const menuWidth: number = menuEl.offsetWidth || 164;
            const menuHeight: number = menuEl.offsetHeight || 110;
            left = Math.min(Math.max(left, 8), Math.max(window.innerWidth - menuWidth - 8, 8));
            top = Math.min(Math.max(top, 8), Math.max(window.innerHeight - menuHeight - 8, 8));

            return { left, top };
        },

        openContextMenu: function(tagId: string|number, tagNm: string, onSearch: () => void, contentType: string = "JRNL_DAY"): void {
            if (!dF.JrnlDayTag.isContextMenuEnabled()) {
                onSearch();
                return;
            }

            const anchorEl: HTMLElement | null = dF.JrnlDayTag.getContextAnchorEl();
            dF.JrnlDayTag.stopCurrentClickEvent();
            dF.JrnlDayTag.highlightContextAnchor(anchorEl);

            const menuEl: HTMLElement = dF.JrnlDayTag.ensureContextMenu();
            dF.JrnlDayTag.contextMenuState = { tagId, tagNm, onSearch, contentType, anchorEl };
            dF.JrnlDayTag.bindContextMenuEvents();

            menuEl.style.display = "block";
            const position: Record<string, number> = dF.JrnlDayTag.getContextMenuPosition(menuEl);
            menuEl.style.left = `${position.left}px`;
            menuEl.style.top = `${position.top}px`;
        },

        hideContextMenu: function(): void {
            const menuEl: HTMLElement | null = dF.JrnlDayTag.contextMenuEl;
            if (menuEl instanceof HTMLElement) menuEl.style.display = "none";

            dF.JrnlDayTag.contextMenuState = null;
            dF.JrnlDayTag.resetContextAnchor();
        },

        runContextMenuSearch: function(): void {
            const currentState: Record<string, any> | null = dF.JrnlDayTag.contextMenuState;
            dF.JrnlDayTag.hideContextMenu();

            if (currentState?.onSearch) currentState.onSearch();
        },

        runContextMenuConfigure: function(): void {
            const currentState: Record<string, any> | null = dF.JrnlDayTag.contextMenuState;
            dF.JrnlDayTag.hideContextMenu();

            if (!currentState) return;
            dF.JrnlDayTag.profileModal(currentState.tagId, currentState.contentType, currentState.tagNm);
        },

        getContentTypeLabel: function(contentType: string): string {
            switch (contentType) {
                case "JRNL_DAY": return "\uC77C\uC790";
                case "JRNL_DIARY": return "\uC77C\uAE30";
                case "JRNL_DREAM": return "\uAFC8";
                case "JRNL_INTRPT": return "\uD574\uC11D";
                default: return contentType;
            }
        },

        getProfileFormData: function(): Record<string, any> {
            const data: Record<string, any> = {};
            $("#tagProfileForm").serializeArray().forEach(function(item: JQuery.NameValuePair): void {
                data[item.name] = item.value;
            });
            return data;
        },

        syncProfileTextClassSelectStyle: function(): void {
            const selectEl: HTMLSelectElement | null = document.querySelector("#tagTextClassCd");
            if (!selectEl) return;

            const selectedOption: HTMLOptionElement | undefined = selectEl.selectedOptions?.[0];
            const textClass: string = selectedOption?.dataset.textClass ?? "";
            selectEl.className = `form-select form-select-solid ${textClass}`.trim();
        },

        initProfileTextClassForm: function(): void {
            const selectEl: HTMLSelectElement | null = document.querySelector("#tagTextClassCd");
            if (!selectEl) return;

            selectEl.addEventListener("change", function(): void {
                dF.JrnlDayTag.syncProfileTextClassSelectStyle();
            });
            dF.JrnlDayTag.syncProfileTextClassSelectStyle();
        },

        profileModal: function(tagId: string|number, contentType: string, tagNm: string): void {
            if (isNaN(Number(tagId)) || cF.util.isEmpty(contentType)) return;

            const url: string = Url.TAG_PROFILES;
            const ajaxData: Record<string, any> = { tagId, contentType };
            cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }

                const viewModel: Record<string, any> = {
                    ...(res.rsltObj ?? {}),
                    tagId: res.rsltObj?.tagId ?? Number(tagId),
                    contentType: res.rsltObj?.contentType ?? contentType,
                    tagNm,
                    contentTypeLabel: dF.JrnlDayTag.getContentTypeLabel(contentType),
                };
                cF.handlebars.modal(viewModel, "tag_profile");
                dF.JrnlDayTag.initProfileTextClassForm();
                $("#tag_profile_del_btn").toggleClass("d-none", cF.util.isEmpty(viewModel.id));
            });
        },

        submitProfile: function(): void {
            const ajaxData: Record<string, any> = dF.JrnlDayTag.getProfileFormData();
            if (isNaN(Number(ajaxData.tagId)) || cF.util.isEmpty(ajaxData.contentType)) return;

            Swal.fire({
                text: Message.get(cF.util.isEmpty(ajaxData.id) ? "view.cnfm.reg" : "view.cnfm.mdf"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                cF.$ajax.post(Url.TAG_PROFILES, ajaxData, function(res: AjaxResponse): boolean {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (!res.rslt) return;

                        $("#tag_profile_modal").modal("hide");
                    });
                    return res.rslt;
                });
            });
        },

        deleteProfileAjax: function(): void {
            const id: string = cF.util.getInputValue("#tagProfileForm [name='id']");
            if (isNaN(Number(id))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.TAG_PROFILE, { id });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): boolean {
                    Swal.fire({ text: res.message }).then(function(): void {
                        if (!res.rslt) return;

                        $("#tag_profile_modal").modal("hide");
                    });
                    return res.rslt;
                });
            });
        },

        /**
         * 상세 모달 호출
         * @param {string|number} tagId - 조회할 태그 ID.
         * @param tagNm 태그 이름
         */
        select: function(tagId: string|number, tagNm: string): void {
            if (dF.JrnlDayTag.isContextMenuEnabled()) {
                dF.JrnlDayTag.openContextMenu(tagId, tagNm, function(): void {
                    dF.JrnlDayTag.dtlModal(tagId, tagNm);
                }, "JRNL_DAY");
                return;
            }

            dF.JrnlDayTag.dtlModal(tagId, tagNm);
        },

        /**
         * 상세 모달 호출
         * @param {string|number} tagId - 조회할 태그 ID.
         * @param tagNm 태그 이름
         */
        dtlModal: function(tagId: string|number, tagNm: string, yy?: string|number): void {
            if (typeof event !== "undefined" && event) event.stopPropagation();
            if (isNaN(Number(tagId))) return;

            ModalHistory.reset();

            const self = this;
            const func: string = arguments.callee.name; // 현재 실행 중인 함수 참조
            const args: any[] = Array.from(arguments); // 함수 인자 배열로 받기

            if (dF.JrnlDay?.viewType === "WEEKLY") {
                const weekStartDt: string = dF.JrnlDayTag.getCurrentWeekStartDt();
                const url: string = cF.util.bindUrl(Url.JRNL_DAY_TAG, { tagId });
                cF.ajax.get(url, { weekStartDt }, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }
                    cF.handlebars.modal({
                        id: tagId,
                        list: res.rsltList,
                        yy: weekStartDt,
                        yearOptions: [{ value: weekStartDt, label: `Week ${weekStartDt}`, selected: true }],
                    }, "jrnl_day_tag_dtl");
                    document.querySelector("#jrnl_day_tag_dtl_modal .header_tag_nm").innerHTML = tagNm;
                    document.querySelector("#jrnl_day_tag_dtl_modal .header_tag_cnt").innerHTML = (res.rsltList?.length ?? 0).toString();
                    KTMenu.createInstances();

                    /* modal history push */
                    ModalHistory.push(self, func, args);
                });
                return;
            }

            const preferredYy: string = dF.JrnlDayTag.getSelectedYy(yy);
            dF.JrnlDayTag.getYyListAjax(tagId, function(yyList: any[]): void {
                const selectedYy: string = dF.JrnlDayTag.normalizeSelectedYy(preferredYy, yyList);
                const url: string = cF.util.bindUrl(Url.JRNL_DAY_TAG, { tagId });
                cF.ajax.get(url, { yy: selectedYy }, function(res: AjaxResponse): void {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return;
                    }
                    cF.handlebars.modal({
                        id: tagId,
                        yy: selectedYy,
                        yearOptions: dF.JrnlDayTag.getYearOptions(selectedYy, yyList),
                        list: res.rsltList,
                        weekMode: false,
                    }, "jrnl_day_tag_dtl");
                    document.querySelector("#jrnl_day_tag_dtl_modal .header_tag_nm").innerHTML = tagNm;
                    document.querySelector("#jrnl_day_tag_dtl_modal .header_tag_cnt").innerHTML = (res.rsltList?.length ?? 0).toString();
                    KTMenu.createInstances();

                    /* modal history push */
                    ModalHistory.push(self, func, args);
                });
            });
        },

        /**
         * 년도 변경
         * @param {string|number} tagId
         * @param {string|number} yy
         */
        changeYy: function(tagId: string|number, yy: string|number): void {
            if (isNaN(Number(tagId))) return;

            const tagNm: string = document.querySelector("#jrnl_day_tag_dtl_modal .header_tag_nm")?.textContent ?? "";
            dF.JrnlDayTag.dtlModal(tagId, tagNm, yy);
        },

        /**
         * 모달 닫기 시 수행할 로직
         */
        closeModal: function(): void {
            /* modal history pop */
            ModalHistory.prev();
        },

        expand: function(obj: HTMLElement): void {
            $(obj).prev(".cn").toggleClass("expanded");
        },

        tagCtgrSyncAjax: function(): void {

        }
    }
})();
