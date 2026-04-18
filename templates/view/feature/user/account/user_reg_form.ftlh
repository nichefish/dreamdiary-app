<#--
  -- 사용자 관리 > 계정 및 권한 관리 > 등록 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (formMode == "regist")!false>
<#assign isMdf = (formMode == "modify")!false>

<@layout.layout toolbar=toolbar!"">

<style>
    @media (max-width: 768px) {
        #idDupChckBtn{
            font-size:12px;
        }
    }
</style>

<div id="user_reg_form_div">
    <#-- Handlebars template renders here -->
</div>

<#include "/view/feature/user/account/_user_reg_form_template.hbs">

<#--include::script modules-->
<script type="text/javascript" src="/js/view/feature/user/account/user_module.js?${releaseDate!}"></script>
<#--include::user_emplym_modules-->
<#include "/view/feature/user/emplym/_user_emplym_reg_template.hbs">
<script type="text/javascript" src="/js/view/feature/user/emplym/user_emplym_module.js?${releaseDate!}"></script>
<#--include::user_profile_modules-->
<#include "/view/feature/user/profile/_user_profile_reg_template.hbs">
<script type="text/javascript" src="/js/view/feature/user/profile/user_profile_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/user/account/user_reg_form.js?${releaseDate!}"></script>

</@layout.layout>
