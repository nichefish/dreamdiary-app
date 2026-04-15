/**
 * draggable.ts
 * 공통 - draggable 관련 함수 모듈
 *
 * @namespace: cF.draggable (노출식 모듈 패턴)
 * @author: nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.draggable = (function(): Module {
    return {
        /**
         * Draggable 컴포넌트 초기화
         * @param selectorSuffix
         * @param {Function} keyExtractor - 각 드래그 가능한 요소의 키를 추출하는 함수. 인자로 (item, index) 받음.
         * @param {string} url - 정렬 순서를 서버에 전송할 URL.
         * @param {Function} [refreshFunc] - 정렬이 성공적으로 완료된 후 호출되는 콜백 함수. (선택적)
         * @returns {Draggable.Sortable} - 드래그 가능한 정렬된 요소들의 인스턴스.
         */
        init: function(selectorSuffix: string = "", keyExtractor: Function, url: string, refreshFunc: Function): Draggable {
            const zoneSelector: string = '.draggable-zone' + selectorSuffix;
            const itemSelector: string = '.sortable-item.draggable' + selectorSuffix;
            const handleSelector: string = '.draggable-handle' + selectorSuffix;

            const containers: NodeListOf<Element> = document.querySelectorAll(zoneSelector);
            if (containers.length === 0) return;

            let initIdxs: Object[] = [];
            const onDragStart: Function = (event: any): void => {
                const container: any = event.data.source.closest(zoneSelector); // 드래그 시작한 요소의 부모 컨테이너
                const source: HTMLElement = event.data.source;
                source.classList.add('dragging');

                // 드래그 전 초기 정렬 순서 저장 (스냅샷)
                initIdxs = Array.from(container.querySelectorAll(itemSelector) as HTMLElement[])
                    .map((el: HTMLElement): string => el.dataset.id);
            };
            const onDragStop: Function = (event: any): void => {
                const container = event.data.source.closest(zoneSelector); // 드래그 시작한 요소의 부모 컨테이너
                const source: HTMLElement = event.data.source;

                setTimeout((): void => {
                    source.classList.remove('dragging');
                    source.classList.add('draggable-modified');

                    // 드래그 후 정렬 순서 저장 (스냅샷)
                    const newIdxs: string[] = Array.from(container.querySelectorAll(itemSelector) as HTMLElement[])
                        .map((el: HTMLElement): string => el.dataset.id);

                    const isChanged: boolean = initIdxs.length !== newIdxs.length || !initIdxs.every((id: string, index: number):  boolean => id === newIdxs[index]);

                    // 정렬 순서 ajax 저장
                    if (isChanged) cF.draggable.sortIdx(selectorSuffix, keyExtractor, url, refreshFunc);
                }, 0); // 지연 시간을 0으로 설정하여 다음 이벤트 루프에서 실행되도록 함
            };

            return new Draggable.Sortable(containers, {
                draggable: zoneSelector + " " + itemSelector,
                handle: zoneSelector + " " + itemSelector + " " + handleSelector,
                mirror: {
                    appendTo: "body",
                    constrainDimensions: true
                },
            }).on('drag:start', onDragStart).on('drag:stop', onDragStop);
        },

        /**
         * 정렬순서 저장
         * @param {string} selectorSuffix - 선택 구분자
         * @param {Function} keyExtractor - 각 sortable item의 key를 추출하는 함수. 인자로 (item, index) 받음.
         * @param {string} url - 서버에 데이터 전송을 위한 URL.
         * @param {Function} [refreshFunc] - 정렬이 성공적으로 완료된 후 호출되는 콜백 함수. (선택적)
         */
        sortIdx: function(selectorSuffix: string, keyExtractor: Function, url: string, refreshFunc?: Function): void {
            const zoneSelector: string = '.draggable-zone' + selectorSuffix;
            const itemSelector: string = '.sortable-item.draggable' + selectorSuffix;
            const sortOrdersData: object[] = [];

            const container: Element = document.querySelector(zoneSelector);
            const items: NodeListOf<Element> = container.querySelectorAll(itemSelector);
            items.forEach((item: HTMLElement, idx: number): void => {
                const key: Function = keyExtractor(item, idx);
                sortOrdersData.push({ ...key, sortOrder: idx });
            });
            const ajaxData: Record<string, any> = { "sortOrders": sortOrdersData };
            cF.$ajax.put(url, ajaxData, function(res: AjaxResponse): void {
                if (!res.rslt) {
                    if (cF.util.isNotEmpty(res.message)) return Swal.fire({ text: res.message });
                }
                (refreshFunc || cF.ui.blockUIReload)();
            }, "block");
        },
    }
})();