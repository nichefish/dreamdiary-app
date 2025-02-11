package io.nicheblog.dreamdiary.global.validator;

/**
 * Regex
 * <pre>
 *  공통 사용 정규식 정의
 * </pre>
 *
 * @author nichefish
 */
public interface Regex {

    /** 한글 정규식 */
    String KOR_REGEX = "(^[가-힣]+)$";

    /** 특수문자 정규식 */
    String SPECIAL_CHAR_ALL_REGEX = "[^\\p{Alnum}\\p{IsHangul}]+";

    /** ID 정규식 */
    String ID_REGEX = "^(?=.*[a-z])[a-z\\d]{5,16}$";

    /** 패스워드 정규식 */
    String PW_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[$@$!%*#?&_])[a-zA-Z\\d$~@$!%*#?&_!]{9,20}$";
    /** 패스워드 정규식 (대문자 포함) */
    String PW_UPPER_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*#?&])[a-zA-Z\\d$~@$!%*#?&!]{9,20}$";
    /** 2차 패스워드 정규식 */
    String SCSC_PW_REGEX = "^(?=.*[a-zA-Z])(?=.*[$~@!%*#?&])[a-zA-Z$~@!%*#?&]{5}$";

    /** ipv4 cidr 정규식 */
    String CIDR_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/(\\d|[1-2]\\d|3[0-2]))?$";

    /** 이메일 정규식 */
    String EMAIL_REGEX = "^([\\w-]+(?:\\.[\\w-]+)*)@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$";
    /** 숫자 정규식 */
    String NUM_REGEX = "[\\d]+";

    /** HTML 태그 (<>...</>로 묶인 영역) 정규식 */
    String HTML_TAG_REGEX = "<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>";

    /* 공격 패턴 :: 아래 패턴과 일치하지 않아야 한다. */

    /** ATTACK_PATTERN */
    String ATTACK_PATTERN = "(\\.\\./)+|(\\./)+";

    /** SIMPLE_XSS_REGEX. %73 %63 %72 %69 %70 %74 */
    String SIMPLE_XSS_REGEX = "((\\%3C)|<)((\\%2F)|\\/)*[a-z0-9\\%]+((\\%3E)|>)";
    String[] SIMPLE_XSS_REGEX_PATTERNS = {
            "[Aa][Ll][Ee][Rr][Tt] *\\(",
            "[Dd][Oo][Cc][Uu][Mm][Ee][Nn][Tt]\\.\\w[^as]", // document.as는 예외 (2011.03.22)
            "\\.[Cc][Oo][Oo][Kk][Ii][Ee]",
            "[Xx][Ss][Ss] *:",
            ": *[Ee][Xx][Pp][Rr][Ee][Ss][Ss][Ii][Oo][Nn]",
            "[Ss][Tt][Yy][Ll][Ee] *=",
            "[Bb][Aa][Cc][Kk][Gg][Rr][Oo][Uu][Nn][Dd] *[:\\.]",
    };
    String SIMPLE_ODS_XSS_REGEX = "((\\%3C)|<)((\\%2F)|\\/)*((%73|s|S|%53)(%63|c|C|%43)(%72|r|R|%52)(%69|i|I|%49)(%70|p|P|%50)(%74|t|T|%54))((\\%3E)|>)";

    /** IMG_XSS_REGEX. */
    String IMG_XSS_REGEX = "((\\%3C)|<)((\\%69)|i|(\\%49))((\\%6D)|m|(\\%4D))((\\%67)|g|(\\%47))[^\\n]+((\\%3E)|>)";
}
