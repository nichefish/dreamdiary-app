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
- **스토어**: `stores/boardPost.ts`
- **레이아웃**: `BoardPostLayout` > `BoardPostList.vue`

### Layout Structure

- 레이아웃: `layout_default.ftlh` (사이드바 없음)
- 툴바: `_board_post_list_header.ftlh`
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
