# 게시판 화면 스펙 (Board Screen Spec)

> 전체 라우트 목록: `docs/migration/vue-screen-overview.md`

## 라우트·화면 매핑

| 화면 | 레거시 URL | Vue route | Vue view | 구현 |
|------|-----------|-----------|----------|------|
| 게시판 목록 | `/app/board/post/list.do` | `/board/:boardKey` | `BoardPostList.vue` | ✓ |

---

## 게시판 목록 (`board-post-list`)

- **Vue SPA**: `/board/:boardKey`
- **Legacy file**: `legacy/templates/view/feature/board/post/board_post_list.ftlh`
- **스토어**: `features/board/stores/boardPost.ts`
- **레이아웃**: `BoardPostLayout` > `BoardPostViewToolbar` + `BoardPostList.vue`
- **i18n**: 목록 조회 오류는 현재 locale의 클라이언트 카탈로그 메시지를 표시한다. 등록·수정·삭제 결과는 서버 `message`를 우선 사용하고, 서버 메시지가 없을 때 현재 locale의 클라이언트 카탈로그 메시지를 표시한다.

### Layout Structure

- 레이아웃: `layout_default.ftlh` (사이드바 없음)
- 툴바: `_board_post_list_header.ftlh`
- Vue SPA: `BoardPostLayout`이 `BoardPostViewToolbar`(등록, `pe-5 mt-3 mb-1`)를 목록 위에 둔다. ASIDE는 없다. 탭용 `mt-5` 빈 여백은 두지 않는다.
- Vue 목록 본문 상단은 breadcrumb와 중복되는 `게시판` 제목을 렌더링하지 않는다. 헤더·검색 카드는 **저널 스레드 목록과 동일 골격**이되 등록은 뷰 툴바로 옮긴다(변경 전: 검색 카드 우측 등록).
  - 상단 행: 태그 클라우드만.
  - 하단 행(`border-top pt-3`): 분류 select + 키워드 input + 검색·초기화 버튼.
  - 검색 카드는 `margin-top: 0`으로 툴바에 붙인다.
- **검색 필터**: `store.filterKeyword`/`filterCategory` 는 이전부터 `fetchList` 가 API(`searchKeyword`/`categoryCode`)로 전송하고 있었으나 입력 UI 가 없어 값을 넣을 수단이 없었다. 검색 실행은 항상 첫 페이지부터 조회하고, 초기화는 두 조건을 비운 뒤 재조회한다.
- **태그 클라우드**: `GET /api/tags?contentType=<boardKey>` 로 조회한다(스레드와 같은 범용 태그 API). 게시판 태그는 `tag_content.ref_content_type` 에 **boardKey**(예: `TEST`)로 저장되며 `ContentType` enum 의 `BOARD` 가 아니다 — 게시물 목록 API 의 `contentType` 규약과 같다. 태그 버튼 클릭 시 해당 태그로 목록을 거르고, 같은 태그를 다시 누르면 해제한다. 태그 조건은 공통 `BaseAttachableSearchParam.tags`(List&lt;Integer&gt;)로 전달하므로 `BoardPostSearchParam` 에 별도 필드가 필요 없다. 태그 클라우드는 게시판 전환(`setBoard`) 시마다 다시 조회하고, 초기화는 태그 선택도 함께 해제한다.
- **행 액션**: 목록 행의 수정·삭제는 저널 스레드 목록·저널 일자와 동일하게 `...` 컨텍스트 메뉴(`data-kt-menu`)로 제공한다(테이블 행 액션 SSOT). `.table-responsive`가 없으므로 overflow portal은 쓰지 않는다. 트리거 `@click.stop` 금지(KTMenu body 위임); 행 상세는 `isMetronicMenuEventTarget` 가드. `store.loading` 종료 시 `reinitMetronicAfterDom()` 재바인딩.
- **분류 선택지**: 분류 코드 그룹은 게시판마다 다르므로(`board.category_group_code`) `GET /api/board/{boardKey}/categories` 로 조회한다(`BoardPostRestController.boardCategoryListAjax`). 관리 화면(코드 관리) API를 우회하지 않는 사용자 화면 전용 읽기 계약이며, 저널 스레드의 `/api/journal/threads/categories` 와 같은 패턴이다. 게시판에 분류 그룹이 지정돼 있지 않으면 서버가 **빈 목록**을 반환하고, 화면은 **분류 select 를 렌더링하지 않는다**. 선택지는 게시판 전환(`setBoard`) 시마다 다시 조회한다.
- 메인 영역:
  - 태그 필터바: `_tag_list_header.ftlh`
  - 특수 버튼: `board == 'cmpyLife'` 조건 시 회사생활 공지사항 안내 버튼 (`#cmpy_life_modal` 트리거)
  - Vue 마운트 루트: `#board_post_list_app.d-none`
  - 카드: `.card.post`
    - 카드 바디: 테이블 (서버사이드 `<#list>` 렌더)
    - 카드 푸터: `_pagination.ftlh`
- 히든 폼: `#procForm` (GET, `id`, `board` hidden)

### Key UI Elements

| Element | Type | Legacy class/id | Data source | Notes |
|---------|------|----------------|-------------|-------|
| 뷰 툴바 | `BoardPostViewToolbar` | Vue 신규 툴바 | `useBoardPostStore.openRegist` | 등록 버튼만. 저널 스레드·결산 액션 행과 동형(`pe-5 mt-3 mb-1`). ASIDE 없음 |
| Vue 태그·검색 카드 | `.card.mb-4` + `margin-top: 0` | Vue 신규 검색 카드 | `/api/tags?contentType=<boardKey>`, `/api/board/{boardKey}/categories` | 태그·분류·제목 검색. 등록은 뷰 툴바로 이동 |
| 회사생활 안내 버튼 | `<button>` | `.btn.btn-sm.btn-light.btn-active-secondary.mx-1` | `board == 'cmpyLife'` | `blink` 애니메이션 + `bi-lightbulb-fill.text-noti` |
| 테이블 | `<table>` | `.table.align-middle.table-row-dashed.fs-small.gy-5.table-fixed.hoverTable.mb-3` | `postList` | Freemarker 서버사이드 렌더 |
| 번호 열 | `<th>` | `.text-center.wb-keepall.w-10.hidden-table` | `post.rnum` | 모바일 숨김 |
| 제목 열 | `<th>` | `.col-lg-8.col-9.text-center.wb-keepall` | `post.title` | 태그 포함 시 `pb-4` |
| 첨부파일 열 | `<th>` | `.col-lg-1.text-center.wb-keepall.hidden-table` | 파일 정보 | 모바일 숨김 |
| 제목 링크 | `<a>` | `.text-dark.vertical-middle.text-underline-dotted` | `post.title`, `post.board`, `post.id`, `post.notionPageId` | 상세 페이지 링크 |
| 카테고리 배지 | `<span>` | `.ctgr-span.ctgr-gray` | `post.ctgrNm` | 제목 앞 표시 |
| 댓글 수 | `@component.list_comment` | `.text-noti.btn-active-warning.cursor-pointer` | `post.comment.cnt` | 댓글 모달 오픈 |
| 신규 배지 | `.badge.border-0.text-white.bg-noti.blink.fs-8.ms-2` | `post.isNew` | N 텍스트 blink |
| 태그 목록 | `@component.list_tag` | `.me-6.fs-7` | `post.tag.list` | `#` 접두사 태그 배지 |
| 모달 보기 버튼 | `@component.list_dtl_modal` | `.badge.badge-secondary.p-2.btn-white.bg-hover-white.blank.blink-slow.float-end` | `post.id` | `bi-stickies.text-noti` |
| 첨부파일 버튼 | `@component.list_file_group` | `.badge.badge-secondary.p-2.btn-white.blink-slow` | `post.fileGroupId` | `bi-file-earmark-arrow-down.text-info` |

### Action Buttons & Interactions

| Action | Trigger | Legacy handler | Expected behavior |
|--------|---------|---------------|-------------------|
| 등록 모달 열기 | 뷰 툴바(`BoardPostViewToolbar`) 등록 버튼 클릭 | `store.openRegist()` | 등록 모달 오픈 |
| 게시글 상세 이동 | 제목 링크 클릭 | `href=${Url.BOARD_POST_DETAIL}?board=...&id=...&notionPageId=...` | 상세 페이지 이동 |
| 상세 모달 열기 | `bi-stickies` 아이콘 클릭 | `window.dispatchEvent(new CustomEvent('board-post:open-detail-modal', { detail: { id: N } }))` | 상세 모달 오픈 |
| 댓글 모달 | 댓글 수 클릭 | `CommentList.modal(id, contentType)` | 댓글 목록 모달 |
| 파일 모달 | 첨부파일 아이콘 클릭 | `FileGroupList.modal(fileGroupId)` | 파일 목록 모달 |
| 태그 상세 | 태그 클릭 | `dF.Tag.dtlModal(tagId)` | 태그 상세 모달 |

### Data Displayed

`postList` 서버 모델로 Freemarker `<#list>` 렌더. 각 행:
- `post.rnum`: 행 번호
- `post.id`: 게시글 ID
- `post.board`: 게시판 구분
- `post.notionPageId`: Notion 페이지 ID (있을 경우)
- `post.title`: 제목
- `post.ctgrNm`: 카테고리명
- `post.isNew`: 신규 여부
- `post.comment.cnt`: 댓글 수
- `post.contentType`: 컨텐츠 타입 (댓글 모달용)
- `post.fileGroupId`: 파일 그룹 ID
- `post.fileGroupInfo.fileRecordList`: 파일 목록 (유무 체크)
- `post.tagStrList`, `post.tag.list`, `tag.tagId`, `tag.ctgr`, `tag.name`: 태그

### Modals opened from this page

| Modal | 파일 | 열리는 조건 |
|-------|------|-----------|
| 게시글 상세 | `_board_post_detail_modal.ftlh` | 모달 아이콘 클릭 (CustomEvent) |
| 댓글 목록 | `_comment_list_modal.ftlh` | 댓글 수 클릭 |
| 파일 목록 | `_file_list_modal.ftlh` | 첨파파일 아이콘 클릭 |

### Special behaviors

- `board == 'cmpyLife'` 조건 분기: 회사생활 게시판이면 안내 버튼 추가 렌더
- 신규 게시글 blink 배지 (`bg-noti blink`)
- 모달 열기가 jQuery가 아닌 `CustomEvent` (`board-post:open-detail-modal`)로 처리 (Vue 전환)
- `BoardPostListApp` Vue 모듈이 헤더/모달 버튼 처리 담당
- 서버사이드 렌더 테이블 + Vue 기반 모달 혼재 구조
