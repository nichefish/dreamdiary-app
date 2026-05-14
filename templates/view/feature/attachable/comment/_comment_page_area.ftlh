<#--
 * 레이아웃 > 댓글 모듈
 * @included_in : 댓글 기능이 있는 페이지들
 * 변경(D): dF.Comment.page(jQuery 기반) + Handlebars 등록 폼 → Vue CommentPageArea 컴포넌트로 전환.
 *          - 등록/수정/삭제 성공 시 blockUIReload(전체 리로드) → 댓글 목록 반응형 갱신으로 개선.
 -->

<#--inject::댓글 페이지 영역 컨텍스트 (CommentPageArea 초기화용)-->
<script type="application/json" id="comment_page_area_data">
{"comments":[<#list post.comment.list![] as c><#sep>,</#sep>{"id":${c.id?c},"content":"${c.content!?js_string}","createdByNm":"${c.createdByNm!?js_string}","createdAt":"${c.createdAt!}","updatedAt":"${c.updatedAt!?js_string}","isCreatedBy":${c.isCreatedBy!false?c},"createdByInfo":{"profileImageUrl":"${(c.createdByInfo.profileImageUrl)!?js_string}"}}</#list>],"refId":${post.id?c},"refContentType":"${post.contentType!?js_string}","actvtyCtgrCd":"${actvtyCtgrCd!?js_string}","currentUser":{"nickname":"${authInfo.nickname!?js_string}","profileImageUrl":"${(authInfo.profileImageUrl)!?js_string}"}}
</script>

<div class="separator separator-dashed mt-8"></div>
<div id="comment_page_area_app"></div>

<#--include::vue module-->
<script type="module" src="/vue/feature/attachable/comment/CommentPageArea.js?${releaseDate!}"></script>