package io.nicheblog.dreamdiary.feature.ai;

/**
 * AI person 테스트 공통 가상 픽스처.
 *
 * <pre>
 *  실명·실제 기록에서 유래하지 않은 가상 인물 상수만 보관한다. person 관련 서비스 테스트가 재사용한다.
 * </pre>
 *
 * @author nichefish
 */
public final class AiPersonTestFixtures {

    private AiPersonTestFixtures() {
    }

    /** 테스트 전용 가상 인물 A (태도/의미 질문 기본 픽스처). */
    public static final String PERSON_A = "민수";
    public static final String PERSON_A_TAG = "[엔서클]#김민수";

    /** 테스트 전용 가상 인물 B (등장/appearance 질문 픽스처). */
    public static final String PERSON_B = "지연";
    public static final String PERSON_B_TAG = "[엔서클]#박지연";
    public static final String PERSON_B_CANONICAL = "박지연";
    public static final String PERSON_B_FALSE_POSITIVE_TAG = "[유명인]#문지연";
}
