<#--
 * clsf(댓글, 태그, 조치, 열람) 모듈 관련 컴포넌트
 * @included_in : lib/components.ftl
 -->

<#--
 * 열람자 정보 표시 영역
 -->
<#macro post_viewer_area post >
    <#if isMngr>
        <div class="row mb-5">
            <div class="col-xl-1">
                <div class="col-form-label fs-6 fw-bold mx-5">열람자</div>
            </div>
            <div class="col-xl-11 col-form-label">
                <#if post.viewer??>
                    <#list post.viewer.list![] as viewer>
                        <span
                            data-bs-toggle="tooltip" data-bs-placement="top" title="${viewer.regstrId!"-"}">
                            ${viewer.regstrNm!"-"}
                        </span>

                        <#if !viewer?is_last>/</#if>
                    </#list>
                </#if>
            </div>
        </div>
        <div class="separator separator-dashed border-gray-300 my-8"></div>
    </#if>
</#macro>

<#--
 * 조치자 정보 표시 영역
 -->
<#macro post_header_managtr_area post >
    <!--end::Foot-->
    <#if post.managt?? && post.managt.list?? && post.managt.list?size gt 1>
        <div class="separator separator-dashed border-gray-300 mb-8"></div>
        <div class="d-flex flex-stack flex-wrap justify-content-start ">
            <!--begin::Author-->
            <div class="d-flex align-items-center me-5">
                <span class="text-gray-400 fs-6 fw-bold lh-1">
                    작업자:
                </span>
            </div>
            <div class="d-flex align-items-center">
                <#list post.managt.list![] as managtr>
                    <!--begin::Name-->
                    <div class="text-gray-600 fs-6 me-4">
                        <#if (managtr.regstrInfo.proflImgUrl)??>
                            <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                <img src="${(managtr.regstrInfo.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
                            </div>
                        </#if>
                        <span class="cursor-help"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="최종 작업일시: &#10;${managtr.regDt!}">
                                ${managtr.regstrNm!"-"}
                            </span>
                        <#if managtr?has_next>, </#if>
                    </div>
                    <!--end::Name-->
                </#list>
            </div>
            <!--end::Author-->
        </div>
    </#if>
</#macro>

<#--
 * 잔디 알림 발송 영역
 -->
<#macro jandi_reg_area labelwidth jandiTopic>
    <div class="row h-50px">
        <div class="col-lg-${labelwidth} col-3 d-flex align-items-center width-33">
            <div class="text-gray-700 fs-6 fw-bolder cursor-help"
                  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="해당 글이 작성되었음을&#10;사내 메신저(JANDI)를 통해&#10;알립니다.">
                잔디 알림 발송
            </div>
        </div>
        <div class="col-lg-1 col-3 d-flex align-items-center width-33">
            <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                <input type="checkbox" name="jandiYn" id="jandiYn" class="form-check-input ms-3"
                       value="Y" />
                <label class="form-check-label fw-bold ms-3" for="jandiYn" id="jandiYnLabel" style="color:gray;">미발송</label>
            </div>
        </div>
        <div class="col-lg-2 col-6 d-flex align-items-center width-33">
            <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
            <div id="trgetTopicSpan" style="display:none;">
                <select name="trgetTopic" id="trgetTopic" class="form-select form-select-solid"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="잔디 메세지를 발송할&#10;토픽(대화방)을 선택합니다.">
                    <#if isDev!false>
                        <option value="TEST">[테스트]</option>
                    </#if>
                    <#list JANDI_TOPIC_CD![] as topic>
                        <#if topic.dtlCd == jandiTopic>
                            <option value="${topic.dtlCd!}" id="${topic.dtlCd!}">[${topic.dtlCdNm!}]</option>
                        </#if>
                    </#list>
                </select>
            </div>
        </div>
    </div>
</#macro>
