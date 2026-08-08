package io.nicheblog.dreamdiary.feature.ai.person;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 인물 질문 분류·focus 토큰 추출 규칙.
 *
 * <p>순수 정적 규칙만 담당한다. RAG 검색·스냅샷·Path C hybrid는 호출자 책임이다.</p>
 */
public final class PersonQueryClassifier {

/** person focus 토큰 최소 길이 */
private static final int PERSON_FOCUS_MIN_TOKEN_LENGTH = 2;
/** person-meaning 질문에서 person focus를 감지하는 문장 힌트 */
private static final String[] PERSON_FOCUS_HINTS = {
        "내 기록", "기록에서", "내 대화",
        "어떤 의미", "무슨 의미",
        "어떤 존재", "어떤 역할",
        "왜 반복", "왜 자주",
        "어떻게 등장", "등장하는",
        "어떻게 생각", "생각하고",
        "어떤 감정", "어떤 마음", "어떤 느낌",
        "어떻게 느끼", "느끼고"
};
/** 인물 토큰 + '~에 대해 뭘 말해' 류 LOOKUP형 질문 힌트 */
private static final String[] PERSON_ABOUT_LOOKUP_HINTS = {
        "에 대해", "뭘 말해", "무엇을 말해", "말해줘", "말해 줘",
        "뭐라고", "뭘 알려", "무엇을 알려",
        "알려줘", "알려 줘", "말해줄", "말해 줄"
};
/** person focus 토큰 추출 시 제외할 불용어 */
private static final Set<String> PERSON_FOCUS_STOPWORDS = Set.of(
        "나는", "너는", "내", "나", "기록",
        "dreamdiary", "Dreamdiary", "AI",
        "의미", "등장", "등장해", "등장하는",
        "무슨", "어떤", "어떻게",
        "역할", "존재", "반복", "자주",
        "통섭", "해석", "요약", "정리",
        "말해줘", "말해", "보여줘",
        "대해", "관련", "전체", "맥락",
        "대화", "느낌", "등장하", "있니"
);

    private PersonQueryClassifier() {
    }

/**
 * 인물 이름이 추출되고 '~에 대해 뭘 말해/알려줘' 류로 묻는 질문인지 확인합니다.
 */
public static boolean isPersonAboutLookupQuery(final String queryText) {
    if (extractPersonFocusTokens(queryText).isEmpty()) {
        return false;
    }
    return StringUtils.containsAny(StringUtils.defaultString(queryText), PERSON_ABOUT_LOOKUP_HINTS);
}
/**
 * query가 person-centric synthesis 질문인지 확인합니다.
 */
public static boolean isPersonMeaningQuery(final String queryText) {
    final String text = StringUtils.defaultString(queryText);
    if (StringUtils.isBlank(text)) return false;
    if (StringUtils.containsAny(text, PERSON_FOCUS_HINTS)) {
        return true;
    }
    return isPersonAboutLookupQuery(text);
}

/**
 * 1인칭 태도·자기인식 질문(나는 X를 어떻게 생각/느끼는지)인지 확인합니다.
 *
 * <p>person-meaning(상징·역할 축·등장 방식)과 구분해 Path C 태도 rich-trust 프롬프트·최소 게이트를 태웁니다.
 * (예전에는 PERSON_STANCE_SCAFFOLD·강경 가드를 태웠으나 Option A 수렴으로 제거됨.)
 * {@code 내 대화에서 X는 어떤 느낌으로 등장}처럼 범위+등장 질문은 false입니다.</p>
 */
public static boolean isPersonAttitudeQuery(final String queryText) {
    final String text = StringUtils.defaultString(queryText);
    if (StringUtils.isBlank(text) || extractPersonFocusTokens(queryText).isEmpty()) {
        return false;
    }
    if (isPersonAppearanceQuery(text)) {
        return false;
    }
    if (!hasExplicitFirstPersonSubjectMarker(text)) {
        return false;
    }
    return StringUtils.containsAny(text,
            "어떻게 생각", "생각하고",
            "어떤 감정", "어떤 마음", "어떤 느낌",
            "어떻게 느끼", "느끼고");
}

/**
 * 기록·대화 속 인물의 등장 방식·느낌·톤을 묻는 질문인지 확인합니다.
 *
 * <p>1인칭 태도 질문과 달리 주어가 인물({@code 지연님은 … 등장})이거나 {@code 내 대화/내 기록} 범위 질문입니다.</p>
 */
public static boolean isPersonAppearanceQuery(final String queryText) {
    final String text = StringUtils.defaultString(queryText);
    if (StringUtils.isBlank(text)) return false;
    if (StringUtils.containsAny(text,
            "등장", "등장하", "나타나", "나오는", "나오", "보이는", "보여")) {
        return true;
    }
    if (StringUtils.containsAny(text, "내 대화", "내 기록", "대화에서", "대화 속", "대화 안")) {
        return !hasExplicitFirstPersonSubjectMarker(text);
    }
    return false;
}

/**
 * {@code 나는/내가} 등 주어가 사용자 자신인 1인칭 표지가 있는지 확인합니다.
 *
 * <p>{@code 내 대화}·{@code 내 기록} 같은 소유 범위만으로는 true가 되지 않습니다.</p>
 */
public static boolean hasExplicitFirstPersonSubjectMarker(final String queryText) {
    return StringUtils.containsAny(StringUtils.defaultString(queryText),
            "나는", "내가", "나의", "나한테", "나에게");
}
/**
 * person focus로 삼을 후보 token을 추출합니다.
 */
public static List<String> extractPersonFocusTokens(final String queryText) {
    final String normalized = StringUtils.defaultString(queryText).replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", " ");
    if (StringUtils.isBlank(normalized)) return List.of();

    final Set<String> seen = new HashSet<>();
    final List<String> tokens = new ArrayList<>();
    for (final String rawToken : normalized.split("\\s+")) {
        final String token = stripTrailingJosa(StringUtils.trimToEmpty(rawToken));
        if (StringUtils.length(token) < PERSON_FOCUS_MIN_TOKEN_LENGTH) continue;
        if (PERSON_FOCUS_STOPWORDS.contains(token)) continue;
        if (!containsReadablePersonToken(token)) continue;
        if (seen.add(token)) {
            tokens.add(token);
        }
    }
    return tokens;
}
/**
 * person 토큰이 로그/기록 텍스트에서도 확인 가능한 리터럴인지 확인합니다.
 */
public static boolean containsReadablePersonToken(final String token) {
    return token.matches(".*[\\p{IsAlphabetic}\\p{IsDigit}].*");
}

/**
 * 흔한 조사를 제거해 해석 대상 이름의 토큰으로 정규화합니다.
 */
public static String stripTrailingJosa(final String token) {
    if (StringUtils.length(token) < PERSON_FOCUS_MIN_TOKEN_LENGTH) return token;

    final String[] suffixes = {
            "님께", "님에게", "님에", "님을", "님를", "님은", "님는", "님이", "님가", "님과", "님와", "님의",
            "에게서", "에게", "에서", "에는", "에게는", "께",
            "한테서", "한테",
            "으로는", "으로", "로는", "로",
            "님", "씨",
            "은", "는", "이", "가", "을", "를", "과", "와", "도", "만", "의"
    };

    String normalized = StringUtils.trimToEmpty(token);
    boolean changed = true;
    while (changed && normalized.length() >= PERSON_FOCUS_MIN_TOKEN_LENGTH) {
        changed = false;
        for (final String suffix : suffixes) {
            if (normalized.length() > suffix.length() + 1 && StringUtils.endsWith(normalized, suffix)) {
                normalized = normalized.substring(0, normalized.length() - suffix.length());
                changed = true;
                break;
            }
        }
    }
    return normalized;
}
public static boolean isPersonLookupQuery(final String queryText) {
    return !extractPersonFocusTokens(queryText).isEmpty();
}
}
