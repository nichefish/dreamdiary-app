<#--
 * user_reqst_verification_code.ftlh
 * 사용자 계정 신청 후 인증번호 발송 email template
 *
 * @author nichefish
 -->

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Dreamdiary 계정 신청 인증번호</title>
</head>
<body>
    <h2 style="padding-left:0.5em;">Dreamdiary 계정 신청 인증번호</h2>
    <div style="color: #303030; padding-left:1em; padding-top:0.5em;">
        <p>안녕하세요, <strong>${recipientName!"user"}</strong>님!</p>
        <p>아래 인증 링크를 클릭하여 계정 신청을 완료해주세요.</p>

        <h3 style="padding:1em;">
            인증 링크: ${authenticationUrl!}
        </h3>

        <p>TODO: 이 인증 링크는 메일 발송 후 10분(${validUntil!}간 유효합니다.</p>
        <p>TODO: 인증 링크 만료시 하단 링크를 클릭하여 재발송 요청이 가능합니다.</p>
        <p>감사합니다.</p>
    </div>
</body>
</html>