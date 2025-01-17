/**
 * table.ts
 * 공통 - table 관련 함수 모듈
 *
 * @namespace: cF.table (노출식 모듈 패턴)
 * @author nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.table = (function(): Module {
    return {
        /**
         * 모든 table 헤더에 클릭 이벤트를 설정하여 해당 열을 정렬합니다.
         */
        initSort: function(): void {
            if (typeof Page === 'undefined') { var Page: Page = {} as any }
            const tables: HTMLCollectionOf<HTMLTableElement> = document.getElementsByTagName("table");
            // 각 테이블에 대해 헤더 클릭 이벤트 설정
            Array.from(tables).forEach((table: HTMLTableElement): void => {
                const headers = table.getElementsByTagName("th");

                // 각 헤더에 대해 클릭 이벤트 설정
                Array.from(headers).forEach((header, j: number): void => {
                    header.onclick = (): void => {
                        cF.table.sort(table, j, Page.tableSortMode);
                        Page.tableSortMode = (Page.tableSortMode === "REVERSE") ? "FORWARD" : "REVERSE";
                    };
                });
            });
        },

        /**
         * 특정 테이블 헤더에 해당하는 열을 정렬합니다.
         * @param {string} tableId - 정렬할 테이블의 ID.
         * @param {number} colIdx - 정렬할 열의 인덱스.
         * @param {string} sortMode - 정렬 방식 ("FORWARD" 또는 "REVERSE").
         */
        sortByIdx: function(tableId: string, colIdx: number, sortMode: string): void {
            const table: HTMLElement = document.getElementById(tableId);
            cF.table.sort(table, colIdx, sortMode);
        },

        /**
         * 테이블(텍스트, 숫자) 정렬 함수
         * @param {HTMLTableElement} table - 정렬할 테이블 요소.
         * @param {number} n - 정렬할 열의 인덱스.
         * @param {string} sortMode - 정렬 방식 ("FORWARD" 또는 "REVERSE").
         */
        sort: function(table: HTMLTableElement, n: number, sortMode: string): void {
            if (!table || !table.tBodies) return;

            const tbody = table.tBodies[0];
            const rows = Array.from(tbody.getElementsByTagName("tr")); // HTMLCollection을 배열로 변환
            if (rows.length < 2) return;

            // 셀에서 값을 추출하여 정리하는 중첩 함수
            const getCellValue = (row, index: number) => {
                const cell = row.getElementsByTagName("td")[index];
                const value = cell.textContent || cell.innerText;
                return isNaN(Number(value.replace(/,/g, ""))) ? value : Number(value.replace(/,/g, ""));
            };
            // 두 값을 비교하는 중첩 함수
            const compareValues = (value1, value2): number => {
                if (value1 < value2) return -1;
                if (value1 > value2) return 1;
                return 0;
            };
            // 행 정렬
            rows.sort((row1, row2): number => {
                const value1 = getCellValue(row1, n);
                const value2 = getCellValue(row2, n);

                if (sortMode === "FORWARD") {
                    return compareValues(value1, value2);
                } else if (sortMode === "REVERSE") {
                    return compareValues(value2, value1);
                }
                return 0;
            });

            // 정렬된 행을 tbody에 다시 추가
            rows.forEach(row => tbody.appendChild(row));
        },


        /**
         * 테이블(추가된 input값: 텍스트, 숫자) 정렬 함수
         * @param {HTMLTableElement} table - 정렬할 테이블 요소.
         * @param {number} n - 정렬할 열의 인덱스.
         * @param {string} sortMode - 정렬 방식 ("FORWARD" 또는 "REVERSE").
         */
        sortReqst: function(table: HTMLTableElement, n: number, sortMode: string): void {
            if (!table || !table.tBodies) return;

            const tbody = table.tBodies[0];
            const rows = Array.from(tbody.getElementsByTagName("tr"));
            if (rows.length < 2) return;

            // 셀에서 값을 추출하여 정리하는 함수
            const getCellValue = (row, index: number) => {
                const cell = row.getElementsByTagName("td")[index];
                const inputValue = cell.getElementsByTagName("input")[0].value;
                return isNaN(Number(inputValue.replace(/,/g, ""))) ? inputValue : Number(inputValue.replace(/,/g, ""));
            };
            // 두 값을 비교하는 함수
            const compareValues = (value1, value2): number => {
                if (value1 < value2) return -1;
                if (value1 > value2) return 1;
                return 0;
            };
            // 정렬
            rows.sort((row1, row2): number => {
                const value1 = getCellValue(row1, n);
                const value2 = getCellValue(row2, n);
                if (sortMode === "FORWARD") {
                    return compareValues(value1, value2);
                } else if (sortMode === "REVERSE") {
                    return compareValues(value2, value1);
                }
                return 0;
            });

            // 정렬된 행을 tbody에 다시 추가
            rows.forEach(row => tbody.appendChild(row));
        },
    }
})();