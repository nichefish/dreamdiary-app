<#--
 * 상세/수정 페이지 관련 컴포넌트
 * @included_in : lib/components.ftl
 -->

<!--
 * 체크박스
 -->
<#macro checkbox param checkboxNm ynLabel ynColor defaultOn  >
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            const ynLabel = "${ynLabel}", ynColor = "${ynColor}";
            cF.ui.chckboxLabel("${checkboxNm}", ynLabel, ynColor);
            const checkboxId = "${checkboxNm}".split[1];
            const separator = "//";
            const noStr = ynLabel.substring(ynLabel.indexOf(separator) + 2);
            const noColor = ynColor.substring(ynColor.indexOf(separator) + 2);
            $("#${checkboxNm}Label").text(noStr).css("color", noColor);
            if ("${param[checkboxNm]!}" == "Y" || "${defaultOn}" === "defaultOn") $("#${checkboxNm}").click();
            document.getElementById("${checkboxNm}").id = checkboxId;
        });
    </script>
    <div class="form-check form-switch form-check-custom form-check-solid">
        <input type="checkbox" name="${checkboxNm}" id="${checkboxNm}" class="form-check-input ms-3"
               value="Y" />
        <label class="form-check-label fw-bold ms-3" for="${checkboxNm}" id="${checkboxNm}Label">
            <!-- Color and Label procs by javascropt -->
        </label>
    </div>
</#macro>