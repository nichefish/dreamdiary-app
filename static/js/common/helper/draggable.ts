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
        init: function(selectorSuffix = "", keyExtractor: Function, url: string, refreshFunc: Function): Draggable {
            console.log("'cF.draggable' module initialized.");

            const containers = document.querySelectorAll(".draggable-zone" + selectorSuffix);
            if (containers.length === 0) return;

            const firstContainer = containers[0];
            let initOrdr = [];

            const onDragStart = (event) => {
                const container = event.data.source.parentElement; // 드래그 시작한 요소의 부모 컨테이너
                event.data.source.classList.add('dragging');

                // 드래그 전 초기 정렬 순서 저장
                initOrdr = Array.from(container.querySelectorAll('.draggable' + selectorSuffix) as HTMLElement[])
                    .map(draggable => draggable.getAttribute("id"));
            };
            const onDragStop = (event) => {
                const container = event.data.source.parentElement; // 드래그 시작한 요소의 부모 컨테이너
                const id = event.data.source.getAttribute("id");
                setTimeout(() => {
                    const newTr = document.querySelector(`tr[data-id='${id}'`) || document.querySelector(`li[data-id='${id}'`);
                    newTr.classList.remove('dragging');
                    newTr.classList.add('draggable-modified');

                    // 드래그 후 정렬 순서 가져오기
                    const newOrdr = Array.from(container.querySelectorAll('.draggable' + selectorSuffix) as HTMLElement[])
                        .map(draggable => draggable.getAttribute("id"));
                    const isOrdrChanged = !initOrdr.every((id, index) => id === newOrdr[index]);

                    // 정렬 순서 ajax 저장
                    if (isOrdrChanged) cF.draggable.sortOrdr(keyExtractor, url, refreshFunc);
                }, 0); // 지연 시간을 0으로 설정하여 다음 이벤트 루프에서 실행되도록 함
            };

            return new Draggable.Sortable(containers, {
                draggable: '.draggable' + selectorSuffix,
                handle: '.draggable' + selectorSuffix + " .draggable-handle" + selectorSuffix,
                mirror: {
                    //appendTo: selector,
                    appendTo: "body",
                    constrainDimensions: true
                },
            }).on('drag:start', onDragStart).on('drag:stop', onDragStop);
        },

        /**
         * 정렬순서 저장
         * @param {Function} keyExtractor - 각 sortable item의 key를 추출하는 함수. 인자로 (item, index) 받음.
         * @param {string} url - 서버에 데이터 전송을 위한 URL.
         * @param {Function} [refreshFunc] - 정렬이 성공적으로 완료된 후 호출되는 콜백 함수. (선택적)
         */
        sortOrdr: function(keyExtractor: Function, url: string, refreshFunc: Function): void {
            const orderData = [];
            document.querySelectorAll('.sortable-item').forEach((item: HTMLElement, index: number): void => {
                const key = keyExtractor(item, index);
                orderData.push({ ...key, "state": { "sortOrdr": index } });
            });
            const ajaxData: Record<string, any> = { "sortOrdr": orderData };
            cF.ajax.jsonRequest(url, 'POST', ajaxData, function(res: AjaxResponse): void {
                if (res.rslt) {
                    (refreshFunc || cF.util.blockUIReload)();
                } else if (cF.util.isNotEmpty(res.message)) {
                    Swal.fire({ text: res.message });
                }
            }, "block");
        },

    }
})();