<#--
 * 휴가계획서(vcatn_papr) > 휴가규칙 Modal
 * @included_in : vcatn_papr_list.ftlh
 * @included_in : vcatn_papr_reg_form.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="vcatn_papr_rule" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "휴가규칙"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <#--begin::Table-->
            <table class="table table-row-bordered table-row-gray-300 gy-2 align-middle fs-small">
                <#--begin::Table head-->
                <thead>
                    <tr class="fw-bolder fs-6 text-gray-700">
                        <th class="text-center w-20">항목</th>
                        <th class="text-center"><@spring.message 'cn'/></th>
                        <th class="text-center w-15">일수</th>
                    </tr>
                </thead>
                <#--begin::Table body-->
                <tbody>
                <tr>
                    <td rowspan="2">유급휴일</td>
                    <td class="ps-0">1주동안 소정근로일을 개근한 사원에 대해서는 일요일을 유급 주휴일로 부여한다.</td>
                    <td>1일/주</td>
                </tr>
                <tr>
                    <td>근로자의 날(5월1일)은 유급휴일로 한다.</td>
                    <td>1일</td>
                </tr>
                <tr>
                    <td rowspan="2">연차휴가</td>
                    <td class="ps-0">1년 초과 근무 시 매2년 마다 1일 추가</td>
                    <td>1일/주</td>
                </tr>
                <tr>
                    <td>근로자의 날(5월1일)은 유급휴일로 한다.</td>
                    <td>1일</td>
                </tr>
                <tr>
                    <td>하기휴가</td>
                    <td class="ps-0">연차휴가의 범위 내에서 자유롭게 사용, 이 경우 연차휴가를 사용한 것으로 한다.</td>
                    <td></td>
                </tr>
                <tr>
                    <td>생리휴가</td>
                    <td class="ps-0">여성의 경우 월 1회 무급 생리휴가 부여</td>
                    <td>1일/월</td>
                </tr>
                <tr>
                    <td>신입직원</td>
                    <td class="ps-0">1년 미만 근무한 직원의 경우 매월 1일의 휴가를 사용할 수 있으며 다음 해에 발생하는 연차휴가에서 차감</td>
                    <td></td>
                </tr>
                <tr>
                    <td rowspan="7">경조휴가<br/>(주말/휴일과 별도)</td>
                    <td class="ps-0">본인의 결혼</td>
                    <td>5일</td>
                </tr>
                <tr>
                    <td>양가 형제 자매 결혼, 양가 4촌 이내 형제 자매 결혼</td>
                    <td>1일</td>
                </tr>
                <tr>
                    <td>배우자의 출산</td>
                    <td>3일</td>
                </tr>
                <tr>
                    <td>본가/처가 부모 사망</td>
                    <td>4일</td>
                </tr>
                <tr>
                    <td>양가 조부모 사망, 직계 외조부모 사망</td>
                    <td>2일</td>
                </tr>
                <tr>
                    <td>형제 또는 자녀 사망</td>
                    <td>3일</td>
                </tr>
                <tr>
                    <td>양가 부모 회갑</td>
                    <td>1일</td>
                </tr>
                <tr>
                    <td rowspan="2">병가휴가</td>
                    <td class="ps-0">업무외 질병•부상 등으로 병가를 신청하는 경우 연간 60일을 초과하지 않는 범위에서 허가할 수 있다.</td>
                    <td rowspan="2">최대 60일</td>
                </tr>
                <tr>
                    <td>이 경우 병가기간은 무급으로 한다.</td>
                </tr>
                <tr>
                    <td rowspan="2">임산부의 보호</td>
                    <td class="ps-0">임신 중의 여성 사원에게 산전과 산후를 통하여 90일의 보호휴가를 준다. 이 경우 반드시 산후에 45일 이상 부여한다.</td>
                    <td rowspan="2">90일</td>
                </tr>
                <tr style="border-bottom: 1px solid #EFF2F5 !important;">
                    <td class="border-bottom">- 고용보험에서 산전•후 휴가급여 지급</td>
                </tr>
                </tbody>
            </table>
            <h4 class="text-start mt-10">[부칙]</h4>
            <#--begin::Table-->
            <table class="fs-small">
                <tr class="pt-10px">
                    <td colspan="3">
                        1. 휴가기간 : 매년 3월 1일 ~ 다음해 2월 말일 까지로 한다.
                    </td>
                </tr>
                <tr class="pt-10px">
                    <td colspan="3">
                        2. 신입직원의 경우 최초 연차휴가의 계산은 입사한 날로부터 2월 28일까지의 기간으로 계산한다.
                    </td>
                </tr>
                <tr class="pt-10px">
                    <td colspan="3">
                        3. 휴가의 신청은 휴가일 3일전 인트라넷을 통하여 신청하는 것을 원칙으로 하며 부득이한 사유가 있는 경우 긴급, 또는 사후 신청도 가능하도록 한다.
                    </td>
                </tr>
                <tr class="pt-10px">
                    <td colspan="3">
                        4. 휴가신청은 사수와 사전에 협의하여 진행한다.
                    </td>
                </tr>
                <tr class="pt-10px">
                    <td colspan="3">
                        5. 총 휴가기간 및 남은 휴가기간은 인트라넷을 통하여 확인할 수 있도록 한다.
                    </td>
                </tr>
                <tr class="pt-10px">
                    <td colspan="3">
                        6. 회사는 연차유급휴가의 사용을 촉진할 수 있다.
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>
