package io.nicheblog.dreamdiary.global.util.crypto;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MaskingModule
 * <pre>
 *  마스킹 처리 유틸리티 모듈
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
class MaskingModule {

    /**
     * 한글이름 가운데 글자 마스킹
     */
    public static String nameMasking(final String name) {
        // 한글만 (영어, 숫자 포함 이름은 제외)
        // Matcher matcher = Pattern.compile(CmmRegex.KOR_REGEX).matcher(name);
        // if (!matcher.find()) return name;
        if (StringUtils.isEmpty(name)) return name;

        // 성씨 빼고 나머지 마스킹 (외자인 경우 끝글자 마스킹)
        int length = name.length();
        String middleNameMasked = name.substring(1, (length > 2) ? length - 1 : length);
        StringBuilder dot = new StringBuilder();
        dot.append("*".repeat(middleNameMasked.length()));

        return name.charAt(0) + middleNameMasked.replace(middleNameMasked, dot.toString()) + (length > 2 ? name.charAt(length - 1) : "");
    }

    /**
     * 휴대폰번호 마스킹(가운데 숫자 4자리 마스킹)
     */
    public static String phoneMasking(final String phoneNo) {
        if (StringUtils.isEmpty(phoneNo)) return phoneNo;

        String regex = "(\\d{2,3})-?(\\d{3,4})-?(\\d{4})$";

        Matcher matcher = Pattern.compile(regex)
                                 .matcher(phoneNo);
        if (matcher.find()) {
            String target = matcher.group(2);
            int length = target.length();
            char[] c = new char[length];
            Arrays.fill(c, '*');

            return phoneNo.replace(target, String.valueOf(c));
        }
        return phoneNo;
    }

    /**
     * 이메일 마스킹(앞3자리 이후 '@'전까지 마스킹)
     */
    public static String emailMasking(final String email) throws Exception {
        if (StringUtils.isEmpty(email)) return email;

        String regex = "\\b(\\s+)+@(\\s+.\\s+)";

        Matcher matcher = Pattern.compile(regex)
                                 .matcher(email);
        if (matcher.find()) {
            String target = matcher.group(1);
            int length = target.length();
            if (length > 3) {
                char[] c = new char[length - 3];
                Arrays.fill(c, '*');

                return email.replace(target, target.substring(0, 3) + String.valueOf(c));
            }
        }
        return email;
    }

    /**
     * 계좌번호 마스킹(뒤 5자리)
     * (계좌번호는 은행마다 규칙이 다르므로 일괄 뒤쪽5자리 마스킹)
     */
    public static String accountNoMasking(final String accountNo) throws Exception {
        if (StringUtils.isEmpty(accountNo)) return accountNo;

        // 계좌번호는 숫자만 파악하므로
        String regex = "(^[0-9]+)$";

        Matcher matcher = Pattern.compile(regex)
                                 .matcher(accountNo);
        if (matcher.find()) {
            int length = accountNo.length();
            if (length > 5) {
                char[] c = new char[5];
                Arrays.fill(c, '*');

                return accountNo.replace(accountNo, accountNo.substring(0, length - 5) + String.valueOf(c));
            }
        }
        return accountNo;
    }

    /**
     * 생년월일 마스킹(8자리)
     */
    public static String birthMasking(final String birthday) throws Exception {
        if (StringUtils.isEmpty(birthday)) return birthday;

        String regex = "^((19|20)\\d\\d)?([-/.])?(0[1-9]|1[012])([-/.])?(0[1-9]|[12][0-9]|3[01])$";

        Matcher matcher = Pattern.compile(regex)
                                 .matcher(birthday);
        if (matcher.find()) {
            return birthday.replace("[0-9]", "*");
        }
        return birthday;
    }

    /**
     * 카드번호 가운데 8자리 마스킹
     */
    public static String cardMasking(final String cardNo) throws Exception {
        if (StringUtils.isEmpty(cardNo)) return cardNo;

        // 카드번호 16자리 또는 15자리 '-'포함/미포함 상관없음
        String regex = "(\\d{4})-?(\\d{4})-?(\\d{4})-?(\\d{3,4})$";

        Matcher matcher = Pattern.compile(regex)
                                 .matcher(cardNo);
        if (matcher.find()) {
            String target = matcher.group(2) + matcher.group(3);
            int length = target.length();
            char[] c = new char[length];
            Arrays.fill(c, '*');

            return cardNo.replace(target, String.valueOf(c));
        }
        return cardNo;
    }

    /**
     * 주소 마스킹 (신주소, 구주소, 도로명 주소 숫자만 전부 마스킹)
     */
    public static String addressMasking(final String address) throws Exception {
        if (StringUtils.isEmpty(address)) return address;

        // 신(구)주소, 도로명 주소
        String regex = "(([가-힣]+(\\d{1,5}|\\d{1,5}(,|.)\\d{1,5}|)+(읍|면|동|가|리))(^구|)((\\d{1,5}(~|-)\\d{1,5}|\\d{1,5})(가|리|)|))([ ](산(\\d{1,5}(~|-)\\d{1,5}|\\d{1,5}))|)|";
        String newRegx = "(([가-힣]|(\\d{1,5}(~|-)\\d{1,5})|\\d{1,5})+(로|길))";

        Matcher matcher = Pattern.compile(regex)
                                 .matcher(address);
        Matcher newMatcher = Pattern.compile(newRegx)
                                    .matcher(address);
        if (matcher.find()) {
            return address.replaceAll("[0-9]", "*");
        } else if (newMatcher.find()) {
            return address.replaceAll("[0-9]", "*");
        }
        return address;
    }
}
