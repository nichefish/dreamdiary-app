<#--
 * 저널 일자(jrnl_day) 목록 조회 Template
 * @included_in : jrnl_day_page.ftlh
 -->

<script type="text/x-handlebars-template" id="jrnl_day_list_template">
    {{#each this}}
        <tr class="bg-hover-secondary" id="{{clCd}}"
            onclick="dF.ClCd.dtlModal('${clCd.clCd!}');">
            <td class="text-center hidden-table">
                <div class="draggable-handle d-flex-center cursor-move pt-2"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="드래그하여&#10;항목을 이동합니다.">
                    <i class="ki-duotone ki-abstract-14 fs-2x">
                        <span class="path1"></span>
                        <span class="path2"></span>
                    </i>
                </div>
            </td>
            <td class="text-start px-6 cursor-pointer fw-bold text-gray-700">
                {{#if (exists clCtgrNm)}}
                    <span class="fw-bolder fs-2 ctgr-span ctgr-imprtc">
                        {{clCtgrNm}}
                    </span>
                {{/if}}
                <span>{{clCdNm}}</span>
                <span class="ms-1">({{clCd}})</span>
            </td>
            <td class="text-start ps-6 hidden-table">
                {{dc}}
            </td>
            <td class="text-center hidden-table">
                {{default dtlCdCnt 0}}
            </td>
            <td class="text-center hidden-table">
                {{#ifYn useYn}}
                    <div class="badge badge-success cursor-pointer"
                         onclick="dF.ClCd.unuseAjax('{{clCd}}');"
                         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                         title="클릭하여 사용 상태를&#10;변경합니다.">
                        <i class="bi bi-check text-light blink-slow"></i><@spring.message 'txt.status.use'/>
                    </div>
                {{else}}
                    <div class="badge badge-secondary btn-white cursor-pointer"
                         onclick="dF.ClCd.useAjax('{{clCd}}');"
                         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                         title="클릭하여 사용 상태를&#10;변경합니다.">
                        <i class="bi bi-x blink-slow"></i><@spring.message 'txt.status.unuse'/>
                    </div>
                {{/ifYn}}
            </td>
            <td class="text-center hidden-table">
                <div class="badge badge-danger badge-outlined cursor-pointer"
                     onclick="dF.ClCd.delAjax(''{{clCd}}');"
                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                     title="<@spring.message 'bs.tooltip.del'/>">
                    <i class="bi bi-trash text-danger fs-7 blink-slow"></i><@spring.message 'txt.comm.del'/>
                </div>
            </td>
        </tr>
    {{else}}
        <tr>
            <td colspan=8" class="text-center"><@spring.message 'view.list.empty'/></td>
        </tr>
    {{/each}}
</script>