package io.nicheblog.dreamdiary.feature.journal.entitycatalog.service;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 인물 mention 맥락에서 first-phase 역할 축({@link JournalEntityRoleType})을 추출한다.
 *
 * <p>{@link JournalEntryEntityRefSyncService}가 저장하는 {@code journal_entry_entity_role}과
 * 채팅 {@code personFocus.topRoles} 집계의 입력이다. LLM 관계 추출기가 아니라
 * 휴리스틱 키워드 매칭이다.</p>
 *
 * <p>변경 전: {@code 조금}/{@code 작업}/{@code 처럼}/{@code 반복} 등 노이즈 단서가
 * TENSION·COLLABORATION·SYMBOLIC_FIGURE를 쉽게 오탄했다.
 * 변경 후: 노이즈 단서 제거·강한 관계 표현 보강, 다중 키워드 시 confidence 소폭 상승.</p>
 */
public final class JournalEntityRoleExtractor {

    private static final double MULTI_HIT_BONUS = 0.03D;
    private static final double MAX_CONFIDENCE = 0.95D;

    private static final RoleLexicon[] LEXICONS = {
            new RoleLexicon(JournalEntityRoleType.COLLABORATION, 0.84D, new String[]{
                    "함께", "협업", "협조", "의논", "도와줘", "도와줬", "도와주",
                    "같이 일", "같이 했", "공동", "회의"
            }),
            new RoleLexicon(JournalEntityRoleType.TENSION, 0.82D, new String[]{
                    "긴장", "불안", "부담", "눈치", "떨림", "불편", "어색",
                    "거리감", "경계", "조마로웠", "마음이 무거웠"
            }),
            new RoleLexicon(JournalEntityRoleType.EVALUATION, 0.83D, new String[]{
                    "평가", "인정", "비교", "검증", "칭찬", "비판",
                    "잘해", "잘했", "잘한다", "청중"
            }),
            new RoleLexicon(JournalEntityRoleType.CARE, 0.8D, new String[]{
                    "위로", "돌봄", "챙겨", "챙김", "염려", "달래", "보살",
                    "걱정", "보호", "돌봐줘", "편하게"
            }),
            new RoleLexicon(JournalEntityRoleType.CONFLICT, 0.86D, new String[]{
                    "싸움", "다툼", "갈등", "화남", "분노", "미움",
                    "다퉜", "싸웠", "대립", "충돌", "말다툼"
            }),
            new RoleLexicon(JournalEntityRoleType.DESIRE, 0.79D, new String[]{
                    "원했", "바람", "가까워", "닿고", "그리움", "동경",
                    "보고 싶", "만나고", "끌려", "끌림", "사랑"
            }),
            new RoleLexicon(JournalEntityRoleType.SYMBOLIC_FIGURE, 0.77D, new String[]{
                    "상징", "대신", "느낌으로", "상징적", "표상",
                    "투영", "그림자", "상징처럼"
            })
    };

    private JournalEntityRoleExtractor() {
    }

    /**
     * mention 역할 맥락에서 역할 축 목록을 추출한다. 매칭 없으면 UNKNOWN 1건.
     *
     * @param roleContextSnippet 인물 언급 주변 맥락 (널/빈 가능)
     * @return 역할 축 목록 (insertion 순서)
     */
    public static List<ExtractedRole> extract(final String roleContextSnippet) {
        final String normalized = StringUtils.lowerCase(StringUtils.defaultString(roleContextSnippet));
        final Map<JournalEntityRoleType, ExtractedRole> roleMap = new LinkedHashMap<>();

        if (StringUtils.isNotBlank(normalized)) {
            for (final RoleLexicon lexicon : LEXICONS) {
                final int hits = countHits(normalized, lexicon.keywords());
                if (hits <= 0) {
                    continue;
                }
                final double confidence = Math.min(
                        MAX_CONFIDENCE,
                        lexicon.baseConfidence() + (MULTI_HIT_BONUS * (hits - 1))
                );
                roleMap.put(
                        lexicon.roleType(),
                        new ExtractedRole(lexicon.roleType(), normalized, confidence)
                );
            }
        }

        if (roleMap.isEmpty()) {
            roleMap.put(
                    JournalEntityRoleType.UNKNOWN,
                    new ExtractedRole(
                            JournalEntityRoleType.UNKNOWN,
                            StringUtils.defaultIfBlank(roleContextSnippet, normalized),
                            0.35D
                    )
            );
        }
        return new ArrayList<>(roleMap.values());
    }

    private static int countHits(final String normalizedContext, final String[] keywords) {
        int hits = 0;
        for (final String keyword : keywords) {
            if (StringUtils.isBlank(keyword)) {
                continue;
            }
            if (StringUtils.contains(normalizedContext, StringUtils.lowerCase(keyword))) {
                hits++;
            }
        }
        return hits;
    }

    /**
     * 추출된 역할 축 1건.
     *
     * @param roleType 역할 축
     * @param evidenceSnippet 근거 맥락
     * @param confidence 0~1 신뢰도
     */
    public record ExtractedRole(
            JournalEntityRoleType roleType,
            String evidenceSnippet,
            double confidence
    ) {
    }

    private record RoleLexicon(
            JournalEntityRoleType roleType,
            double baseConfidence,
            String[] keywords
    ) {
    }
}
