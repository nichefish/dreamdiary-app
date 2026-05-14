/**
 * df-global.d.ts
 * dF 글로벌 객체 값 선언.
 *
 * 변경(tag 모듈 Vue 전환, Sub-phase C):
 *   - tag_module.ts 가 제거되면서 `var dF = {} as any` 선언도 사라졌다.
 *   - global.d.ts 의 `declare namespace dF` 만 남으면 TS2708(namespace → value 사용 불가)가 발생한다.
 *   - 별도 파일에 `declare var dF: any` 를 추가해 namespace 선언과 병합시킨다.
 *   - TypeScript 는 같은 컴파일 컨텍스트 내에서 namespace 와 var 선언을 병합한다.
 */
declare var dF: any;