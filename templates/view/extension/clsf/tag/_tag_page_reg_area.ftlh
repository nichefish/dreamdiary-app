<#--
 * 레이아웃 > 태그 모듈
 -->

<#--begin::Row-->
<div class="row">
    <div>
        <label class="mb-2" for="tagListStr">
            <span class="text-gray-700 fs-6 fw-bolder">태그</span>
            <span class="text-gray-500 fs-9 mx-2">※ 태그는 최대 15개, 50자까지 추가 가능합니다.</span>
        </label>
    </div>
    <div class="col-xl-12 text-sm-start" id="tagSpan">
        <input type="text" name="tag.tagListStr" id="tagListStr" class="form-control form-control-solid no-space"
               value="<#if post.tag??>${post.tag.tagListStrWithCtgr!}</#if>" maxlength="1000" autocomplete="off" />
        <div class="d-flex pt-2 gap-2">
            <div id="tag_ctgr_select_div" style="display: none; position: relative;">
                <select id="tag_ctgr_select" class="form-select orm-select-solid py-2">
                    <option value="custom">직접입력</option>
                </select>
            </div>
            <div id="tag_ctgr_div" style="display:none;">
                <input type="text" id="tag_ctgr" class="form-control form-control-sm form-control-solid w-200px"
                       value="" placeholder="태그 카테고리를 입력합니다." maxlength="50">
            </div>
        </div>
    </div>
</div>
