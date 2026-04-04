/**
 * menu_module.ts
 * 메뉴 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.Menu = (function(): dfModule {
    return {
        initialized: false,
        mainSwappable: null,
        subSwappable: null,

        /**
         * initializes module.
         */
        init: function(): void {
            if (dF.Menu.initialized) return;

            dF.Menu.initialized = true;
            console.log("'dF.Menu' module initialized.");
        },

        /**
         * form init
         * @param {Record<string, any> = {}} obj - 폼에 바인딩할 데이터
         */
        initForm: function(obj: Record<string, any> = {}): void {
            /* show modal */
            cF.handlebars.modal(obj, "menu_reg");

            /* jquery validation */
            cF.validate.validateForm("#menuRegForm", dF.Menu.regAjax);
            cF.validate.toUpperCase("#menuRegForm #menuLabel");
        },

        /**
         * 메인 Draggable 컴포넌트 init
         */
        initMainDraggable: function(): void {
            const keyExtractor: Function = (item: HTMLElement): Object => ({ "menuNo": Number(item.dataset.id) });
            dF.Menu.mainSwappable = cF.draggable.init("-main", keyExtractor, Url.MENUS_IDX);
        },

        /**
         * 컨테이너 내부에서 정렬 가능한 자식 요소 목록을 반환한다.
         * @param container 정렬 대상 컨테이너
         * @param itemSelector 정렬 대상 요소 selector
         * @returns selector에 매칭되는 자식 HTMLElement 배열
         */
        getSortableChildren: function(container: HTMLElement, itemSelector: string): HTMLElement[] {
            if (!container) return [];

            return Array.from(container.children)
                .filter((el: Element): boolean => el.matches(itemSelector)) as HTMLElement[];
        },

        /**
         * 현재 컨테이너의 정렬 순서를 dataset.id 기준으로 추출한다.
         * @param container 정렬 대상 컨테이너
         * @param itemSelector 정렬 대상 요소 selector
         * @returns 순서 배열 (menu id 리스트)
         */
        getSortableOrder: function(container: HTMLElement, itemSelector: string): string[] {
            return dF.Menu.getSortableChildren(container, itemSelector)
                .map((item: HTMLElement): string => item.dataset.id);
        },

        /**
         * 두 정렬 상태가 동일한지 비교한다. (길이 + 각 인덱스별 값 비교)
         * @param beforeOrder 이전 순서
         * @param afterOrder 현재 순서
         * @returns 동일 여부
         */
        isSameOrder: function(beforeOrder: string[], afterOrder: string[]): boolean {
            if (beforeOrder.length !== afterOrder.length) return false;

            return beforeOrder.every((id: string, index: number): boolean => id === afterOrder[index]);
        },

        /**
         * 특정 트리 그룹(부모 메뉴 기준)의 정렬 상태를 서버 전송용 payload로 구성한다.
         * @param container 그룹 컨테이너 (부모 메뉴 기준)
         * @param itemSelector 정렬 대상 selector
         * @returns upperMenuNo + 정렬된 item 리스트
         */
        buildTreeGroupPayload: function(container: HTMLElement, itemSelector: string): Record<string, any> | null {
            if (!container) return null;

            const upperMenuNo: number = Number(container.dataset.parentMenuNo);
            if (isNaN(upperMenuNo)) return null;

            const items: Record<string, number>[] = dF.Menu.getSortableChildren(container, itemSelector)
                .map((item: HTMLElement, idx: number): Record<string, number> => ({
                    "menuNo": Number(item.dataset.id),
                    "idx": idx,
                }));

            return {
                "upperMenuNo": upperMenuNo,
                "items": items,
            };
        },

        /**
         * 서브 메뉴 이동/정렬 변경 시 트리 구조를 서버에 반영한다.
         * DOM 읽기 없이 sortable:sorted 이벤트에서 캡처한 순서 배열을 그대로 사용한다.
         * @param movedItem 이동된 요소
         * @param sourceContainer 기존 컨테이너
         * @param targetContainer 이동된 컨테이너
         * @param sortedOrder 캡처된 최종 정렬 순서 (menuNo string 배열)
         */
        sortSubTreeByOrder: function(
            movedItem: HTMLElement,
            sourceContainer: HTMLElement,
            targetContainer: HTMLElement,
            sortedOrder: string[]
        ): void {
            const itemSelector: string = ".sortable-item.draggable-sub";
            const movedMenuNo: number = Number(movedItem.dataset.id);
            const sourceUpperMenuNo: number = Number(movedItem.dataset.upperMenuNo);
            const targetUpperMenuNo: number = Number(targetContainer?.dataset.parentMenuNo);
            if (isNaN(movedMenuNo) || isNaN(sourceUpperMenuNo) || isNaN(targetUpperMenuNo)) return;

            const groups: Record<string, any>[] = [];

            // target 그룹: sortedOrder 그대로 사용 (DOM 안 읽음)
            const targetItems: Record<string, number>[] = sortedOrder.map(
                (id: string, idx: number): Record<string, number> => ({
                    "menuNo": Number(id),
                    "idx": idx,
                })
            );
            groups.push({ "upperMenuNo": targetUpperMenuNo, "items": targetItems });

            // 부모가 바뀐 경우: source 그룹도 추가 (movedItem 제외하고 DOM 읽기)
            if (sourceContainer !== targetContainer) {
                const sourceItems: Record<string, number>[] = dF.Menu.getSortableChildren(sourceContainer, itemSelector)
                    .filter((el: HTMLElement): boolean => Number(el.dataset.id) !== movedMenuNo)
                    .map((el: HTMLElement, idx: number): Record<string, number> => ({
                        "menuNo": Number(el.dataset.id),
                        "idx": idx,
                    }));
                groups.push({ "upperMenuNo": sourceUpperMenuNo, "items": sourceItems });
            }

            const ajaxData: Record<string, any> = {
                "movedMenuNo": movedMenuNo,
                "sourceUpperMenuNo": sourceUpperMenuNo,
                "targetUpperMenuNo": targetUpperMenuNo,
                "groups": groups,
            };

            console.log("ajaxData:", JSON.stringify(ajaxData));

            cF.$ajax.put(Url.MENUS_TREE, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) return Swal.fire({ text: res.message });
                }
                cF.ui.blockUIReload();
            }, "block");
        },

        /**
         * 서브 메뉴 드래그앤드롭 기능 초기화
         * - 드래그 시작 시 기존 상태 저장
         * - 드래그 종료 시 변경 여부 판단 후 서버 반영
         */
        initSubDraggable: function(): void {
            const zoneSelector: string = ".draggable-zone-sub";
            const itemSelector: string = ".sortable-item.draggable-sub";
            const handleSelector: string = ".draggable-handle-sub";
            const containers: NodeListOf<Element> = document.querySelectorAll(zoneSelector);
            if (containers.length === 0) return;

            let sourceContainer: HTMLElement = null;
            let targetContainer: HTMLElement = null;
            let sourceOrder: string[] = [];
            let sortedOrder: string[] = [];      // 추가
            let sortedContainer: HTMLElement = null;  // 추가

            const onDragStart: Function = (event: any): void => {
                const source: HTMLElement = event.data.source;
                sourceContainer = source.closest(zoneSelector) as HTMLElement;
                targetContainer = sourceContainer;
                sourceOrder = dF.Menu.getSortableOrder(sourceContainer, itemSelector);
                sortedOrder = [...sourceOrder];  // 초기값 복사
                sortedContainer = sourceContainer;
                source.classList.add("dragging");
            };

            const onSorted: Function = (event: any): void => {
                const newContainer: HTMLElement = event.data.newContainer as HTMLElement;
                const source: HTMLElement = event.data.dragEvent.data.source as HTMLElement;
                const movedId: string = source.dataset.id;

                // newContainer 기준 현재 순서를 oldIndex/newIndex로 재구성
                const currentOrder: string[] = dF.Menu.getSortableOrder(newContainer, itemSelector)
                    .filter((id: string): boolean => id !== movedId);  // mirror 제외 방어

                // newIndex 위치에 movedId 삽입
                currentOrder.splice(event.data.newIndex, 0, movedId);

                sortedOrder = currentOrder;
                sortedContainer = newContainer;
                targetContainer = newContainer;
            };

            const onDragStop: Function = (event: any): void => {
                const source: HTMLElement = event.data.source;
                const movedAcrossParent: boolean = sourceContainer !== targetContainer;
                const isChanged: boolean = movedAcrossParent || !dF.Menu.isSameOrder(sourceOrder, sortedOrder);

                source.classList.remove("dragging");

                if (!isChanged) return;

                // sortedOrder 기반으로 payload 구성
                dF.Menu.sortSubTreeByOrder(source, sourceContainer, targetContainer, sortedOrder);
            };

            dF.Menu.subSwappable = new Draggable.Sortable(containers, {
                draggable: zoneSelector + " " + itemSelector,
                handle: zoneSelector + " " + itemSelector + " " + handleSelector,
                mirror: {
                    appendTo: "body",
                    constrainDimensions: true,
                },
            })
            .on("drag:start", onDragStart)
            .on("sortable:sorted", onSorted)
            .on("drag:stop", onDragStop);
        },



        /**
         * 등록 모달 호출
         */
        regModal: function(menuTyCd: string, upperMenuNo: number, upperMenuNm: string): void {
            event.stopPropagation();

            const obj: Record<string, any> = { "menuTyCd": menuTyCd, "upperMenuNo": upperMenuNo, "upperMenuNm": upperMenuNm };
            /* initialize form. */
            dF.Menu.initForm(obj);
        },

        /**
         * 아이콘 새로고침
         */
        refreshIcon: function(): void {
            const iconElmt: HTMLInputElement = document.querySelector("#menuRegForm #icon");
            if (!iconElmt) return;

            const menuIconDiv: HTMLElement = document.querySelector("#menuRegForm #menu_icon_div");
            if (menuIconDiv) menuIconDiv.innerHTML = iconElmt.value;
        },

        /**
         * 하위메뉴 존재여부에 따라 url 영역 표시
         */
        toggleUrlSpan: function(obj: object): void {
            const menuSubExtendTyCd = $(obj).val();
            if (menuSubExtendTyCd !== "NO_SUB") {
                $("#url_div").addClass("d-none");
            } else {
                $("#url_div").removeClass("d-none");
            }
        },

        /**
         * form submit
         */
        submit: function(): void {
            $("#menuRegForm").submit();
        },

        /**
         * 메뉴 정보 등록/수정 (Ajax)
         */
        regAjax: function(): void {
            Swal.fire({
                text: Message.get("view.cnfm.save"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const menuNo = cF.util.getInputValue("#menuRegForm #menuNo");
                const isMdf: boolean = cF.util.isNotEmpty(menuNo);
                const url: string = isMdf ? cF.util.bindUrl(Url.MENU, { menuNo }) : Url.MENUS;
                const ajaxData: Record<string, any> = cF.util.getJsonFormData("#menuRegForm");
                cF.$ajax.post(url, ajaxData, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 수정 모달 호출
         */
        mdfModal: function(menuNo: string|number): void {
            if (isNaN(Number(menuNo))) return;

            const url: string = cF.util.bindUrl(Url.MENU, { menuNo });
            cF.ajax.get(url, null, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                    return;
                }
                const { rsltObj } = res;
                /* initialize form. */
                dF.Menu.initForm(rsltObj);
            });
        },

        /**
         * 사용 상태 변경 (Ajax)
         */
        toggleUseAjax: function(menuNo: string|number): void {
            if (isNaN(Number(menuNo))) return;

            const item: HTMLElement = document.querySelector(`li.menu-item[data-id='${menuNo}']`);
            if (!item) console.warn("item does not exists.");
            const currentUseYn: string = item.dataset.useYn;
            const nextUseYn: string = currentUseYn === "Y" ? "N" : "Y";

            const url: string = cF.util.bindUrl(Url.MENU, { menuNo });
            const ajaxData: Record<string, any> = { "useYn": nextUseYn };
            cF.$ajax.patch(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) return Swal.fire({ text: res.message });
                }
                cF.ui.blockUIReload();
            });
        },

        /**
         * 삭제 (Ajax)
         * @param {string|number} menuNo - 메뉴 번호.
         */
        delAjax: function(menuNo: number): void {
            if (isNaN(Number(menuNo))) return;

            Swal.fire({
                text: Message.get("view.cnfm.del"),
                showCancelButton: true,
            }).then(function(result: SwalResult): void {
                if (!result.value) return;

                const url: string = cF.util.bindUrl(Url.MENU, { menuNo });
                cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
                    Swal.fire({ text: res.message })
                        .then(function(): void {
                            if (res.rslt) cF.ui.blockUIReload();
                        });
                }, "block");
            });
        },

        /**
         * 목록 검색
         */
        search: function(): void {
            $("#listForm #pageNo").val(1);
            cF.form.blockUISubmit("#listForm", Url.LOG_ACTVTY_LIST + "?actionTyCd=SEARCH");
        },
    }
})();
